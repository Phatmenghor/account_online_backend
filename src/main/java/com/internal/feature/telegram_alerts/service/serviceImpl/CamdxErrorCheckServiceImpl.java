package com.internal.feature.telegram_alerts.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.feature.telegram_alerts.config.TelegramService;
import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.camdx.dto.CamdxValidateNidRequest;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.telegram_alerts.service.ErrorAlertsCamdxService;
import com.internal.utils.constants.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamdxErrorCheckServiceImpl implements ErrorAlertsCamdxService {

    private final TelegramService telegramService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AccountOnlineReportLogService accountOnlineReportLogService;

    @Override
    public void checkValidationResponse(JsonNode response, CamdxValidateNidRequest request) {
        String idNumber = request.getIdNumber();
        log.info("Checking CAMDX validation result for ID: {}", idNumber);

        try {
            if (response == null || response.isEmpty()) {
                log.warn("Empty CAMDX response. Nothing to check for ID {}", idNumber);
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

            sendTelegram(response, request, score, incorrectFields, idNumber, message, errorCode);

        } catch (Exception e) {
            log.error("Error while checking CAMDX response for ID {}", idNumber, e);
            telegramService.sendMarkdownMessage("Error parsing CAMDX response: " + escapeMarkdown(e.getMessage()));
        }
    }

    private void sendTelegram(JsonNode response, CamdxValidateNidRequest request, double score, List<String> incorrectFields, String idNumber, String message, int errorCode) {
        boolean shouldAlert = score < 1 || (incorrectFields != null && !incorrectFields.isEmpty());

        if (shouldAlert) {
            log.warn("Validation issue detected for ID {} (score={}, incorrectFields={})", idNumber, score, incorrectFields);

            accountOnlineReportLogService.saveLogReport(idNumber, OpenAccStatusEnum.FAILURE, ErrorMessage.CAMDX_VALIDATE);

            try {
                sendErrorAlert(request, message, errorCode, score, incorrectFields);
            } catch (Exception e) {
                log.error("Failed to send Telegram notification, but logs were created: {}", e.getMessage());
            }

        } else {
            log.info("Validation passed successfully for ID {}", idNumber);
        }
    }

    private String convertObjectToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize request to JSON", e);
            return obj.toString();
        }
    }

    @Override
    public void sendInfraErrorAlertFromException(CamdxValidateNidRequest request, String rawMessage) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        accountOnlineReportLogService.saveLogReport(request.getIdNumber(), OpenAccStatusEnum.FAILURE, ErrorMessage.CAMDX_VALIDATE);

        String errorCode = "Unknown";
        String errorMessage = "Unknown";

        try {
            String jsonString = rawMessage;

            int firstBrace = rawMessage.indexOf("{");
            int lastBrace = rawMessage.lastIndexOf("}");
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                jsonString = rawMessage.substring(firstBrace, lastBrace + 1);
            }

            JsonNode json = objectMapper.readTree(jsonString);
            errorCode = json.path("error").isMissingNode() ? "Unknown" : json.path("error").asText();
            errorMessage = json.path("message").isMissingNode() ? "Unknown" : json.path("message").asText();
        } catch (Exception e) {
            errorMessage = rawMessage;
        }

        errorMessage = escapeMarkdown(errorMessage);

        String nidText = (request.getIdNumber() != null && !request.getIdNumber().isEmpty())
                ? "`" + escapeMarkdown(request.getIdNumber())
                : "Missing";

        StringBuilder sb = buildTelegramInfo(request, errorCode, errorMessage, nidText, formatter);

        telegramService.sendMarkdownMessage(sb.toString());
    }

    private StringBuilder buildTelegramInfo(CamdxValidateNidRequest request, String errorCode, String errorMessage, String nidText, DateTimeFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        sb.append("*CAMDX / MIDDLEWARE FAILURE*").append("\n")
                .append("--------------------").append("\n")
                .append("Error Code: ").append(errorCode).append("\n")
                .append("Error Message: ").append(errorMessage).append("\n\n")
                .append("NID: ").append(nidText).append("\n")
                .append("--------------------").append("\n")
                .append("Time: ").append(LocalDateTime.now(ZoneId.of("Asia/Phnom_Penh")).format(formatter)).append("\n")
                .append("Issue: MOI / CAMDX unreachable or infrastructure failure.");
        return sb;
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
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
        String formattedIncorrect = getFormattedIncorrect(incorrectFields);
        StringBuilder sb = buildTelegramErrorAlertInfo(request, message, score, formattedIncorrect, formatter);

        telegramService.sendMarkdownMessage(sb.toString());
    }

    private StringBuilder buildTelegramErrorAlertInfo(CamdxValidateNidRequest request, String message, Double score, String formattedIncorrect, DateTimeFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        sb.append("*CAMDX VALIDATION ERROR*").append("\n")
                .append("--------------------").append("\n")
                .append("Status: ").append(escapeMarkdown(message)).append("\n\n")
                .append("NID: `").append(escapeMarkdown(request.getIdNumber())).append("`\n")
                .append("Score: `").append(String.format("%.2f", score)).append("`\n")
                .append("Incorrect Fields:\n").append(formattedIncorrect).append("\n")
                .append("--------------------").append("\n")
                .append("Request Info\n")
                .append("Name KH: ").append(escapeMarkdown(request.getLastNameKh())).append(" ").append(escapeMarkdown(request.getFirstNameKh())).append("\n")
                .append("Name EN: ").append(escapeMarkdown(request.getLastNameEn())).append(" ").append(escapeMarkdown(request.getFirstNameEn())).append("\n")
                .append("DOB: ").append(escapeMarkdown(request.getDob())).append("\n")
                .append("Gender: ").append(escapeMarkdown(request.getGender())).append("\n")
                .append("Issued: ").append(escapeMarkdown(request.getIssuedDate())).append("\n")
                .append("Expired: ").append(escapeMarkdown(request.getExpiredDate())).append("\n")
                .append("--------------------").append("\n")
                .append("Time: ").append(LocalDateTime.now(ZoneId.of("Asia/Phnom_Penh")).format(formatter)).append("\n")
                .append("Please recheck NID / submission.");
        return sb;
    }

    private static String getFormattedIncorrect(List<String> incorrectFields) {
        if (incorrectFields != null && !incorrectFields.isEmpty()) {
            StringBuilder sbIncorrect = new StringBuilder();
            for (String field : incorrectFields) {
                sbIncorrect.append("- ").append(field).append("\n");
            }
            return sbIncorrect.toString().trim();
        } else {
            return "None";
        }
    }
}
