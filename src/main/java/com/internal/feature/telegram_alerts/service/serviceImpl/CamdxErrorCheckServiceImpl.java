package com.internal.feature.telegram_alerts.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.camdx.dto.CamdxValidateNidRequest;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.telegram_alerts.config.TelegramService;
import com.internal.feature.telegram_alerts.service.ErrorAlertsCamdxService;
import com.internal.utils.constants.AppConstants;
import com.internal.utils.constants.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamdxErrorCheckServiceImpl implements ErrorAlertsCamdxService {

    private final TelegramService telegramService;
    private final AccountOnlineReportLogService accountOnlineReportLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // =====================================================
    // MAIN VALIDATION CHECK
    // =====================================================
    @Override
    public void checkValidationResponse(JsonNode response, CamdxValidateNidRequest request) {

        String idNumber = request.getIdNumber();
        log.info("Checking CAMDX validation result for ID: {}", idNumber);

        try {

            if (response == null || response.isEmpty()) {
                log.warn("Empty CAMDX response for ID {}", idNumber);
                return;
            }

            int errorCode = response.path("error").asInt(0);
            String message = response.path("message").asText("Unknown");

            // Ignore specific messages
            if (shouldIgnoreError(message)) {
                log.info("Ignoring message: {}", message);
                return;
            }

            // =============================
            // INFRA ERROR (API ERROR)
            // =============================
            if (errorCode != 0) {

                log.error("CAMDX API ERROR for ID {} - ErrorCode: {} | Message: {}", idNumber, errorCode, message);

                accountOnlineReportLogService.saveLogReport(
                        idNumber,
                        OpenAccStatusEnum.FAILURE,
                        ErrorMessage.CAMDX_VALIDATE);
                return;
            }

            // =============================
            // BUSINESS VALIDATION CHECK
            // =============================
            JsonNode data = response.path("data");

            double score = data.path("score").asDouble(1.0);

            List<String> incorrectFields = objectMapper.convertValue(
                    data.path("incorrectFields"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

            boolean validationFailed = score < 1 ||
                    (incorrectFields != null && !incorrectFields.isEmpty());

            if (validationFailed) {

                log.warn("CAMDX VALIDATION FAILURE for ID {} | Score: {} | IncorrectFields: {}",
                        idNumber, score, incorrectFields);

                accountOnlineReportLogService.saveLogReport(
                        idNumber,
                        OpenAccStatusEnum.FAILURE,
                        ErrorMessage.CAMDX_VALIDATE);

                // Always send validation failures (score mismatch or field mismatches need review)
                log.info("Sending Telegram alert for validation failure - requires human review");
                sendValidationFailureAlert(request, score, incorrectFields);
            } else {
                log.info("✓ CAMDX VALIDATION SUCCESS for ID {} - no alert sent", idNumber);
            }

        } catch (Exception e) {

            log.error("Unexpected exception during CAMDX validation", e);

            sendInfraErrorAlertFromException(request, e.getMessage());
        }
    }

    // =====================================================
    // INFRA FAILURE FROM EXCEPTION
    // =====================================================
    @Override
    public void sendInfraErrorAlertFromException(CamdxValidateNidRequest request, String rawMessage) {

        log.error("CAMDX EXCEPTION for ID {} | Message: {}", request.getIdNumber(), rawMessage);

        accountOnlineReportLogService.saveLogReport(
                request.getIdNumber(),
                OpenAccStatusEnum.FAILURE,
                ErrorMessage.CAMDX_VALIDATE);
    }

    // =====================================================
    // TELEGRAM VALIDATION FAILURE - Detailed for Dev Team
    // =====================================================
    private void sendValidationFailureAlert(CamdxValidateNidRequest request,
            double score,
            List<String> incorrectFields) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Phnom_Penh"));
        String formattedIncorrect = formatIncorrectFields(incorrectFields);

        // SIMPLE MESSAGE FOR OPERATIONS/MONITORING CHANNEL
        StringBuilder simpleMsg = new StringBuilder();
        simpleMsg.append("*CAMDX VALIDATION FAILURE*\n")
                .append("--------------------\n")
                .append("Status: *FAILURE*\n\n")
                .append("NID: `").append(escapeMarkdown(request.getIdNumber())).append("`\n")
                .append("Score: `").append(String.format("%.2f", score)).append("`\n")
                .append("Incorrect Fields:\n").append(formattedIncorrect).append("\n")
                .append("--------------------\n")
                .append("Request Info\n")
                .append("Name KH: ").append(escapeMarkdown(request.getLastNameKh()))
                .append(" ").append(escapeMarkdown(request.getFirstNameKh())).append("\n")
                .append("Name EN: ").append(escapeMarkdown(request.getLastNameEn()))
                .append(" ").append(escapeMarkdown(request.getFirstNameEn())).append("\n")
                .append("DOB: ").append(escapeMarkdown(request.getDob())).append("\n")
                .append("Gender: ").append(escapeMarkdown(request.getGender())).append("\n")
                .append("Issued: ").append(escapeMarkdown(request.getIssuedDate())).append("\n")
                .append("Expired: ").append(escapeMarkdown(request.getExpiredDate())).append("\n")
                .append("Phone Number: ").append(escapeMarkdown(request.getPhoneNumber())).append("\n")
                .append("--------------------\n")
                .append("Time: ").append(now.format(formatter)).append("\n")
                .append("Please recheck NID / submission.");

        telegramService.sendMarkdownAccountOnlineMonitorMessage(simpleMsg.toString());
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================
    private String formatIncorrectFields(List<String> incorrectFields) {

        if (incorrectFields == null || incorrectFields.isEmpty()) {
            return AppConstants.FIELD_NONE;
        }

        StringBuilder sb = new StringBuilder();

        for (String field : incorrectFields) {
            sb.append(AppConstants.BULLET_PREFIX)
                    .append(field)
                    .append(AppConstants.NEW_LINE);
        }

        return sb.toString().trim();
    }

    private String escapeMarkdown(String text) {

        if (text == null)
            return "";

        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }

    /**
     * Filter out non-critical errors that don't need Telegram alerts.
     * Only critical failures (middleware/connection issues) should be reported.
     * Regular validation failures and "not found" errors are handled through logs only.
     */
    private boolean shouldIgnoreError(String message) {

        if (message == null || message.isEmpty())
            return false;

        String lower = message.toLowerCase();

        // IGNORE: These are expected cases that don't need alerts
        List<String> ignoreList = new ArrayList<>();
        ignoreList.add("id not found");
        ignoreList.add("nid not found");
        ignoreList.add("no record found");
        ignoreList.add("invalid id");
        ignoreList.add("no data found");
        ignoreList.add("record not exist");
        ignoreList.add("does not exist");
        ignoreList.add("not match");
        ignoreList.add("information not found");

        return ignoreList.stream().anyMatch(lower::contains);
    }

    /**
     * Check if this is a critical infrastructure error that requires immediate notification.
     * Returns true if error should be sent to Telegram.
     */
    private boolean isCriticalInfraError(String errorMessage) {
        if (errorMessage == null)
            return false;

        String lower = errorMessage.toLowerCase();

        // CRITICAL ERRORS: Connection, network, service issues
        List<String> criticalPatterns = new ArrayList<>();
        criticalPatterns.add("connection");
        criticalPatterns.add("timeout");
        criticalPatterns.add("moi");
        criticalPatterns.add("middleware");
        criticalPatterns.add("server error");
        criticalPatterns.add("service unavailable");
        criticalPatterns.add("network");
        criticalPatterns.add("socket");
        criticalPatterns.add("refused");
        criticalPatterns.add("unreachable");

        return criticalPatterns.stream().anyMatch(lower::contains);
    }
}
