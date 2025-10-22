package com.internal.feature.telegram_alerts.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.feature.camdx.dto.CamdxValidateNidRequest;
import com.internal.feature.telegram_alerts.service.ErrorAlertsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamdxErrorCheckService implements ErrorAlertsService {

    private final TelegramService telegramService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void checkValidationResponse(JsonNode response, CamdxValidateNidRequest request) {
        String idNumber = request.getIdNumber();
        log.info("Checking CAMDX validation result for ID: {}", idNumber);

        try {
            if (response == null || response.isEmpty()) {
                log.warn("Empty CAMDX response. Nothing to check.");
                return;
            }

            int errorCode = response.path("error").asInt(0);
            String message = response.path("message").asText("Unknown");

            log.info("CAMDX response message: '{}' (error code: {})", message, errorCode);

            if (shouldIgnoreError(message)) {
                log.info("Ignoring non-critical message: {}", message);
                return;
            }

            JsonNode data = response.path("data");
            double score = data.path("score").asDouble(1.0);

            List<String> incorrectFields = objectMapper.convertValue(
                    data.path("incorrectFields"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            boolean shouldAlert = score < 1 || (incorrectFields != null && !incorrectFields.isEmpty());

            if (shouldAlert) {
                log.warn("Validation issue detected for ID {} (score={}, incorrectFields={})", idNumber, score, incorrectFields);
                sendErrorAlert(request, message, errorCode, score, incorrectFields);
            } else {
                log.info("Validation passed successfully for ID {} ✅", idNumber);
            }

        } catch (Exception e) {
            log.error("Error while checking CAMDX response", e);
            telegramService.sendMarkdownMessage("⚠️ Error parsing CAMDX response: " + escapeMarkdown(e.getMessage()));
        }
    }

    public void sendInfraErrorAlertFromException(CamdxValidateNidRequest request, String rawMessage) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String errorCode = "Unknown";
        String errorMessage = "Unknown";

        // Try to extract inner JSON if present
        try {
            String jsonString = rawMessage;

            // If the message contains JSON inside quotes, extract it
            int firstBrace = rawMessage.indexOf("{");
            int lastBrace = rawMessage.lastIndexOf("}");
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                jsonString = rawMessage.substring(firstBrace, lastBrace + 1);
            }

            JsonNode json = objectMapper.readTree(jsonString);
            errorCode = json.path("error").isMissingNode() ? "Unknown" : json.path("error").asText();
            errorMessage = json.path("message").isMissingNode() ? "Unknown" : json.path("message").asText();
        } catch (Exception e) {
            // If parsing fails, fallback to raw message
            errorMessage = rawMessage;
        }

        // Escape Markdown for Telegram
        errorMessage = escapeMarkdown(errorMessage);

        String nidText = (request.getIdNumber() != null && !request.getIdNumber().isEmpty())
                ? "`" + escapeMarkdown(request.getIdNumber()) + "`  ← tap to copy"
                : "Missing";

        StringBuilder sb = new StringBuilder();
        sb.append("🚨 *CAMDX / MIDDLEWARE FAILURE*").append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━").append("\n");
        sb.append("📬 *Error Code:* ").append(errorCode).append("\n");
        sb.append("📬 *Error Message:* ").append(errorMessage).append("\n\n");
        sb.append("🪪 *NID:* ").append(nidText).append("\n");
        sb.append("⚙️ *App:* ").append(request.getApplicationName() != null ? escapeMarkdown(request.getApplicationName()) : "Unknown").append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━").append("\n");
        sb.append("🕒 *Time:* ").append(LocalDateTime.now().format(formatter)).append("\n");
        sb.append("🔍 *Issue:* MOI / CAMDX unreachable or infrastructure failure.");

        telegramService.sendMarkdownMessage(sb.toString());
    }

    /** Escape Markdown special characters for Telegram */
    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }


    private boolean shouldIgnoreError(String message) {
        if (message == null || message.isEmpty()) return false;

        List<String> ignoreList = new ArrayList<>();
        ignoreList.add("id not found");
        ignoreList.add("nid not found");
        ignoreList.add("no record found");
        ignoreList.add("invalid id");
        ignoreList.add("no data found");


        String lower = message.toLowerCase();
        return ignoreList.stream().anyMatch(lower::contains);
    }

    private void sendErrorAlert(CamdxValidateNidRequest request, String message,
                                int errorCode, Double score, List<String> incorrectFields) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Incorrect fields shown as plain text (not copyable)
        String formattedIncorrect = (incorrectFields != null && !incorrectFields.isEmpty())
                ? String.join(", ", incorrectFields)  // just plain text
                : "None";

        StringBuilder sb = new StringBuilder();
        sb.append("🚨 *CAMDX VALIDATION ERROR*").append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━").append("\n");

        sb.append("📬 *Error:* ").append(escapeMarkdown(message)).append("\n\n");

        // Only NID is copyable
        sb.append("🪪 *NID:* `").append(escapeMarkdown(request.getIdNumber())).append("`  ← (tap to copy)\n");
        sb.append("⚙️ *App:* ").append(escapeMarkdown(request.getApplicationName())).append("\n");
        sb.append("📉 *Score:* `").append(String.format("%.2f", score)).append("`\n");
        sb.append("❗ *Incorrect Fields:* ").append(escapeMarkdown(formattedIncorrect)).append("\n"); // plain text
        sb.append("━━━━━━━━━━━━━━━━━━").append("\n");

        sb.append("📋 *Request Info*\n");
        sb.append("• KH: ").append(escapeMarkdown(request.getLastNameKh())).append(" ").append(escapeMarkdown(request.getFirstNameKh())).append("\n");
        sb.append("• EN: ").append(escapeMarkdown(request.getLastNameEn())).append(" ").append(escapeMarkdown(request.getFirstNameEn())).append("\n");
        sb.append("• DOB: ").append(escapeMarkdown(request.getDob())).append("\n");
        sb.append("• Gender: ").append(escapeMarkdown(request.getGender())).append("\n");
        sb.append("• Issued: ").append(escapeMarkdown(request.getIssuedDate())).append("\n");
        sb.append("• Expired: ").append(escapeMarkdown(request.getExpiredDate())).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━").append("\n");

        sb.append("🕒 ").append(LocalDateTime.now().format(formatter)).append("\n");
        sb.append("🔍 Please recheck NID / submission.");

        telegramService.sendMarkdownMessage(sb.toString());
    }
}
