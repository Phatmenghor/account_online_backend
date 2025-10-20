package com.internal.feature.telegram_alerts.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.feature.telegram_alerts.model.NidValidationCache;
import com.internal.feature.telegram_alerts.repository.NidValidationCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CamdxErrorCheckService {

    private final NidValidationCacheRepository cacheRepository;
    private final TelegramService telegramService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void checkLatestValidationError() {
        NidValidationCache cache = cacheRepository.findTopByOrderByCreatedAtDesc();
        if (cache == null) return;

        try {
            JsonNode response = objectMapper.readTree(cache.getOriginal_response());

            // ✅ If CAMDX returns an error field → check the error message
            if (response.has("error")) {
                int errorCode = response.path("error").asInt();
                String message = response.path("message").asText();

                // 🔸 Ignore user-level errors like "ID not found"
                if (shouldIgnoreError(message)) {
                    return;
                }

                sendErrorAlert(cache, message, errorCode, null, null);
                return;
            }

            // ✅ Extract data from success response
            JsonNode data = response.path("data");
            double score = data.path("score").asDouble(1.0);
            List<String> incorrectFields = objectMapper.convertValue(
                    data.path("incorrectFields"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            boolean hasError = score < 1 || (incorrectFields != null && !incorrectFields.isEmpty());
            if (hasError) {
                sendErrorAlert(cache, "Some fields failed validation", 0, score, incorrectFields);
            }

        } catch (Exception e) {
            telegramService.sendMessage("⚠️ Error parsing CAMDX response: " + e.getMessage());
        }
    }

    /**
     * Ignore non-critical messages (e.g., ID not found)
     */
    private boolean shouldIgnoreError(String message) {
        if (message == null || message.isEmpty()) return false;

        List<String> ignoreList = new ArrayList<>();
        ignoreList.add("id not found");
        ignoreList.add("nid not found");
        ignoreList.add("no record found");
        ignoreList.add("invalid id");
        ignoreList.add("no data found");

        String lowerMessage = message.toLowerCase();
        for (String ignore : ignoreList) {
            if (lowerMessage.contains(ignore)) {
                return true;
            }
        }
        return false;
    }


    private void sendErrorAlert(NidValidationCache cache, String message, int errorCode,
                                Double score, List<String> incorrectFields) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append("🚨 *CAMDX VALIDATION ALERT*").append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━").append("\n");
        sb.append("⚙️ *Application:* ").append(cache.getApplication_name()).append("\n");
        sb.append("🪪 *ID Number:* ").append(cache.getId_number()).append("\n");
        sb.append("👤 *Name (EN):* ").append(cache.getFirst_name_en()).append(" ").append(cache.getLast_name_en()).append("\n");
        sb.append("👤 *Name (KH):* ").append(cache.getFirst_name_kh()).append(" ").append(cache.getLast_name_kh()).append("\n");
        sb.append("🎂 *DOB:* ").append(cache.getDob()).append("\n");
        sb.append("⚧ *Gender:* ").append(cache.getGender()).append("\n\n");

        sb.append("📉 *Validation Result*").append("\n");
        if (score != null) sb.append("• *Score:* `").append(String.format("%.2f", score)).append("`\n");
        if (incorrectFields != null && !incorrectFields.isEmpty())
            sb.append("• *Incorrect Fields:* `").append(incorrectFields).append("`\n");
        if (errorCode != 0)
            sb.append("• *Error Code:* ").append(errorCode).append("\n");

        sb.append("\n🕒 *Checked At:* ").append(cache.getCreated_at().format(formatter)).append("\n");
        sb.append("💾 *Source:* nid_validation_cache").append("\n");
        sb.append("📬 *Message:* ").append(message).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━").append("\n");
        sb.append("🔍 *Summary:* One or more validation fields failed. Please review the NID or recheck CAMDX.");

        telegramService.sendMarkdownMessage(sb.toString());
    }
}
