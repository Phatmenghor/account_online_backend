package com.internal.feature.telegram_alerts.service;

import com.internal.feature.telegram_alerts.config.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Centralized monitoring service for all critical operations in the Account Opening system.
 * Sends detailed alerts to Dev Team Telegram channel for easy tracking and debugging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final TelegramService telegramService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Account Opening Flow Events
     */
    public void logAccountOpeningStarted(String legalId, String nidImage, String selfieImage) {
        StringBuilder msg = new StringBuilder();
        msg.append("🟢 *ACCOUNT OPENING STARTED*\n")
                .append("═══════════════════════════════════════\n")
                .append("├─ Legal ID: `").append(escapeMarkdown(legalId)).append("`\n")
                .append("├─ NID Image: `").append(escapeMarkdown(nidImage)).append("`\n")
                .append("├─ Selfie Image: `").append(escapeMarkdown(selfieImage)).append("`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`\n")
                .append("═══════════════════════════════════════");
        sendToDevTeam(msg.toString());
    }

    public void logAccountOpeningStepProgress(String legalId, String stepName, boolean success, String details) {
        String emoji = success ? "✅" : "❌";
        StringBuilder msg = new StringBuilder();
        msg.append(emoji).append(" *ACCOUNT OPENING STEP: ").append(stepName).append("*\n")
                .append("─────────────────────────────────\n")
                .append("├─ Legal ID: `").append(escapeMarkdown(legalId)).append("`\n")
                .append("├─ Status: `").append(success ? "SUCCESS" : "FAILED").append("`\n");

        if (details != null && !details.isEmpty()) {
            msg.append("├─ Details: `").append(escapeMarkdown(details)).append("`\n");
        }

        msg.append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logAccountOpeningCompleted(String legalId, String cif, String khrAccount, String usdAccount, long durationMs) {
        StringBuilder msg = new StringBuilder();
        msg.append("🎉 *ACCOUNT OPENING COMPLETED SUCCESSFULLY*\n")
                .append("═══════════════════════════════════════\n")
                .append("*CUSTOMER DETAILS:*\n")
                .append("├─ Legal ID: `").append(escapeMarkdown(legalId)).append("`\n")
                .append("├─ CIF: `").append(escapeMarkdown(cif)).append("`\n\n")
                .append("*CREATED ACCOUNTS:*\n")
                .append("├─ KHR Account: `").append(escapeMarkdown(khrAccount)).append("`\n")
                .append("├─ USD Account: `").append(escapeMarkdown(usdAccount)).append("`\n\n")
                .append("*EXECUTION:*\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("├─ Timestamp: `").append(getCurrentDateTime()).append("`\n")
                .append("└─ Status: `✅ COMPLETED`\n")
                .append("═══════════════════════════════════════");

        sendToDevTeam(msg.toString());
    }

    public void logAccountOpeningFailed(String legalId, String stepName, String errorMessage, Exception exception) {
        StringBuilder msg = new StringBuilder();
        msg.append("❌ *ACCOUNT OPENING FAILED*\n")
                .append("═══════════════════════════════════════\n")
                .append("*ERROR INFORMATION:*\n")
                .append("├─ Legal ID: `").append(escapeMarkdown(legalId)).append("`\n")
                .append("├─ Failed Step: `").append(escapeMarkdown(stepName)).append("`\n")
                .append("├─ Error Type: `").append(exception.getClass().getSimpleName()).append("`\n")
                .append("├─ Error Message: `").append(escapeMarkdown(errorMessage)).append("`\n");

        if (exception.getCause() != null) {
            msg.append("├─ Root Cause: `").append(escapeMarkdown(exception.getCause().getMessage())).append("`\n");
        }

        msg.append("└─ Time: `").append(getCurrentDateTime()).append("`\n")
                .append("═══════════════════════════════════════\n")
                .append("*ACTION REQUIRED:*\n")
                .append("• Review error logs for details\n")
                .append("• Check service connectivity (T24, AML, CAMDX)\n")
                .append("• Verify customer data submission\n")
                .append("• Contact operations if persistent");

        sendToDevTeam(msg.toString());
    }

    /**
     * T24 Banking Service Events
     */
    public void logT24ServiceCall(String operation, String customerId, long durationMs, boolean success) {
        String emoji = success ? "✅" : "❌";
        StringBuilder msg = new StringBuilder();
        msg.append(emoji).append(" *T24 SERVICE CALL*\n")
                .append("├─ Operation: `").append(escapeMarkdown(operation)).append("`\n")
                .append("├─ Customer ID: `").append(escapeMarkdown(customerId)).append("`\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("├─ Status: `").append(success ? "SUCCESS" : "FAILED").append("`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logT24Error(String operation, String customerId, String errorMessage) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔴 *T24 SERVICE ERROR*\n")
                .append("─────────────────────────────────\n")
                .append("├─ Operation: `").append(escapeMarkdown(operation)).append("`\n")
                .append("├─ Customer ID: `").append(escapeMarkdown(customerId)).append("`\n")
                .append("├─ Error: `").append(escapeMarkdown(errorMessage)).append("`\n")
                .append("├─ Service: `T24 Banking`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`\n")
                .append("\n*ACTION REQUIRED:*\n")
                .append("• Check T24 service availability\n")
                .append("• Verify authentication credentials\n")
                .append("• Review T24 logs for details");

        sendToDevTeam(msg.toString());
    }

    /**
     * AML Processing Events
     */
    public void logAmlCheckStarted(String legalId, String customerName) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔍 *AML CHECK STARTED*\n")
                .append("├─ Legal ID: `").append(escapeMarkdown(legalId)).append("`\n")
                .append("├─ Customer: `").append(escapeMarkdown(customerName)).append("`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logAmlCheckCompleted(String legalId, String riskLevel, double score, long durationMs) {
        String emoji = "LOW".equalsIgnoreCase(riskLevel) ? "✅" : "⚠️";
        StringBuilder msg = new StringBuilder();
        msg.append(emoji).append(" *AML CHECK COMPLETED*\n")
                .append("├─ Legal ID: `").append(escapeMarkdown(legalId)).append("`\n")
                .append("├─ Risk Level: `").append(riskLevel).append("`\n")
                .append("├─ Score: `").append(String.format("%.2f", score)).append("`\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    /**
     * Database Events
     */
    public void logDatabaseConnection(String datasource, boolean success, long durationMs) {
        String emoji = success ? "✅" : "❌";
        StringBuilder msg = new StringBuilder();
        msg.append(emoji).append(" *DATABASE CONNECTION TEST*\n")
                .append("├─ Datasource: `").append(escapeMarkdown(datasource)).append("`\n")
                .append("├─ Status: `").append(success ? "CONNECTED" : "FAILED").append("`\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        if (!success) {
            msg.append("\n*ACTION REQUIRED:*\n")
                    .append("• Check database server status\n")
                    .append("• Verify connection pool settings\n")
                    .append("• Review network connectivity");
        }

        sendToDevTeam(msg.toString());
    }

    public void logDatabaseError(String datasource, String operation, String errorMessage) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔴 *DATABASE ERROR*\n")
                .append("─────────────────────────────────\n")
                .append("├─ Datasource: `").append(escapeMarkdown(datasource)).append("`\n")
                .append("├─ Operation: `").append(escapeMarkdown(operation)).append("`\n")
                .append("├─ Error: `").append(escapeMarkdown(errorMessage)).append("`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`\n")
                .append("\n*ACTION REQUIRED:*\n")
                .append("• Check database logs\n")
                .append("• Verify connection pool status\n")
                .append("• Monitor database performance");

        sendToDevTeam(msg.toString());
    }

    /**
     * Authentication/Access Events
     */
    public void logUserAuthentication(String username, String ipAddress, boolean success) {
        String emoji = success ? "✅" : "❌";
        StringBuilder msg = new StringBuilder();
        msg.append(emoji).append(" *USER AUTHENTICATION*\n")
                .append("├─ Username: `").append(escapeMarkdown(username)).append("`\n")
                .append("├─ IP Address: `").append(escapeMarkdown(ipAddress)).append("`\n")
                .append("├─ Status: `").append(success ? "SUCCESS" : "FAILED").append("`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logSuspiciousActivity(String username, String ipAddress, String activityType, String description) {
        StringBuilder msg = new StringBuilder();
        msg.append("⚠️ *SUSPICIOUS ACTIVITY DETECTED*\n")
                .append("═══════════════════════════════════════\n")
                .append("├─ Username: `").append(escapeMarkdown(username)).append("`\n")
                .append("├─ IP Address: `").append(escapeMarkdown(ipAddress)).append("`\n")
                .append("├─ Activity Type: `").append(escapeMarkdown(activityType)).append("`\n")
                .append("├─ Description: `").append(escapeMarkdown(description)).append("`\n")
                .append("└─ Time: `").append(getCurrentDateTime()).append("`\n")
                .append("═══════════════════════════════════════\n")
                .append("*ACTION REQUIRED:*\n")
                .append("• Verify user identity\n")
                .append("• Review recent activities\n")
                .append("• Check for unauthorized access attempts");

        sendToDevTeam(msg.toString());
    }

    /**
     * General System Events
     */
    public void logExternalApiCall(String apiName, String endpoint, long durationMs, int statusCode, boolean success) {
        String emoji = success ? "✅" : "❌";
        StringBuilder msg = new StringBuilder();
        msg.append(emoji).append(" *EXTERNAL API CALL*\n")
                .append("├─ API: `").append(escapeMarkdown(apiName)).append("`\n")
                .append("├─ Endpoint: `").append(escapeMarkdown(endpoint)).append("`\n")
                .append("├─ Status Code: `").append(statusCode).append("`\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logPerformanceAlert(String componentName, long durationMs, long thresholdMs) {
        StringBuilder msg = new StringBuilder();
        msg.append("⚡ *SLOW PERFORMANCE ALERT*\n")
                .append("├─ Component: `").append(escapeMarkdown(componentName)).append("`\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("├─ Threshold: `").append(thresholdMs).append("ms`\n")
                .append("├─ Exceeded by: `").append(durationMs - thresholdMs).append("ms`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`\n")
                .append("\n*ACTION REQUIRED:*\n")
                .append("• Review component logic\n")
                .append("• Check database query performance\n")
                .append("• Monitor resource utilization");

        sendToDevTeam(msg.toString());
    }

    /**
     * Helper Methods
     */
    private void sendToDevTeam(String message) {
        try {
            telegramService.sendDetailedErrorToDevTeam(message);
        } catch (Exception e) {
            log.debug("Monitoring alert not sent: {}", e.getMessage());
        }
    }

    private String getCurrentTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Phnom_Penh")).format(TIME_FORMATTER);
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Phnom_Penh")).format(DATE_FORMATTER);
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }
}
