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
        // Success logs disabled - only errors sent to dev team
    }

    public void logAccountOpeningStepProgress(String legalId, String stepName, boolean success, String details) {
        if (success) {
            return; // Only send errors to dev team
        }
        StringBuilder msg = new StringBuilder();
        msg.append("*Account Online - Step Failed: ").append(stepName).append("*\n")
                .append("--------------------\n")
                .append("- Legal ID: `").append(escapeMarkdown(legalId)).append("`\n");

        if (details != null && !details.isEmpty()) {
            msg.append("- Details: `").append(escapeMarkdown(details)).append("`\n");
        }

        msg.append("- Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logAccountOpeningFailed(String legalId, String stepName, String errorMessage, Exception exception) {
        StringBuilder msg = new StringBuilder();
        msg.append("*Account Online - FAILED*\n")
                .append("--------------------\n")
                .append("- Legal ID: `").append(escapeMarkdown(legalId)).append("`\n")
                .append("- Failed Step: `").append(escapeMarkdown(stepName)).append("`\n")
                .append("- Error Type: `").append(exception.getClass().getSimpleName()).append("`\n")
                .append("- Error Message: `").append(escapeMarkdown(errorMessage)).append("`\n");

        if (exception.getCause() != null) {
            msg.append("- Root Cause: `").append(escapeMarkdown(exception.getCause().getMessage())).append("`\n");
        }

        msg.append("- Time: `").append(getCurrentDateTime()).append("`\n")
                .append("--------------------");

        sendToDevTeam(msg.toString());
    }

    /**
     * T24 Banking Service Events
     */
    public void logT24ServiceCall(String operation, String customerId, long durationMs, boolean success) {
        if (success) {
            return; // Only send errors to dev team
        }
        StringBuilder msg = new StringBuilder();
        msg.append("*T24 SERVICE CALL FAILED*\n")
                .append("├─ Operation: `").append(escapeMarkdown(operation)).append("`\n")
                .append("├─ Customer ID: `").append(escapeMarkdown(customerId)).append("`\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logT24Error(String operation, String customerId, String errorMessage) {
        StringBuilder msg = new StringBuilder();
        msg.append("*T24 SERVICE ERROR*\n")
                .append("─────────────────────────────────\n")
                .append("├─ Operation: `").append(escapeMarkdown(operation)).append("`\n")
                .append("├─ Customer ID: `").append(escapeMarkdown(customerId)).append("`\n")
                .append("├─ Error: `").append(escapeMarkdown(errorMessage)).append("`\n")
                .append("├─ Service: `T24 Banking`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    /**
     * AML Processing Events
     */
    public void logAmlCheckStarted(String legalId, String customerName) {
        // Success logs disabled - only errors sent to dev team
    }

    public void logAmlCheckCompleted(String legalId, String riskLevel, double score, long durationMs) {
        // Success logs disabled - only errors sent to dev team
    }

    /**
     * Database Events
     */
    public void logDatabaseConnection(String datasource, boolean success, long durationMs) {
        if (success) {
            return; // Only send errors to dev team
        }
        StringBuilder msg = new StringBuilder();
        msg.append("*DATABASE CONNECTION FAILED*\n")
                .append("├─ Datasource: `").append(escapeMarkdown(datasource)).append("`\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logDatabaseError(String datasource, String operation, String errorMessage) {
        StringBuilder msg = new StringBuilder();
        msg.append("*DATABASE ERROR*\n")
                .append("─────────────────────────────────\n")
                .append("├─ Datasource: `").append(escapeMarkdown(datasource)).append("`\n")
                .append("├─ Operation: `").append(escapeMarkdown(operation)).append("`\n")
                .append("├─ Error: `").append(escapeMarkdown(errorMessage)).append("`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    /**
     * Authentication/Access Events
     */
    public void logUserAuthentication(String username, String ipAddress, boolean success) {
        if (success) {
            return; // Only send errors to dev team
        }
        StringBuilder msg = new StringBuilder();
        msg.append("*USER AUTHENTICATION FAILED*\n")
                .append("├─ Username: `").append(escapeMarkdown(username)).append("`\n")
                .append("├─ IP Address: `").append(escapeMarkdown(ipAddress)).append("`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logSuspiciousActivity(String username, String ipAddress, String activityType, String description) {
        StringBuilder msg = new StringBuilder();
        msg.append("*SUSPICIOUS ACTIVITY DETECTED*\n")
                .append("═══════════════════════════════════════\n")
                .append("├─ Username: `").append(escapeMarkdown(username)).append("`\n")
                .append("├─ IP Address: `").append(escapeMarkdown(ipAddress)).append("`\n")
                .append("├─ Activity Type: `").append(escapeMarkdown(activityType)).append("`\n")
                .append("├─ Description: `").append(escapeMarkdown(description)).append("`\n")
                .append("└─ Time: `").append(getCurrentDateTime()).append("`\n")
                .append("═══════════════════════════════════════");

        sendToDevTeam(msg.toString());
    }

    /**
     * General System Events
     */
    public void logExternalApiCall(String apiName, String endpoint, long durationMs, int statusCode, boolean success) {
        if (success) {
            return; // Only send errors to dev team
        }
        StringBuilder msg = new StringBuilder();
        msg.append("*EXTERNAL API CALL FAILED*\n")
                .append("├─ API: `").append(escapeMarkdown(apiName)).append("`\n")
                .append("├─ Endpoint: `").append(escapeMarkdown(endpoint)).append("`\n")
                .append("├─ Status Code: `").append(statusCode).append("`\n")
                .append("├─ Duration: `").append(durationMs).append("ms`\n")
                .append("└─ Time: `").append(getCurrentTime()).append("`");

        sendToDevTeam(msg.toString());
    }

    public void logPerformanceAlert(String componentName, long durationMs, long thresholdMs) {
        // Performance alerts disabled - prevents API slowdown from Telegram sends
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
