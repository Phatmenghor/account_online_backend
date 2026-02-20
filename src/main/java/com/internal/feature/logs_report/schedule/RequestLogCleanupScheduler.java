package com.internal.feature.logs_report.schedule;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.repository.AmlStatusRepository;
import com.internal.feature.logs_report.repository.AccountOnlineFinalRepository;
import com.internal.feature.logs_report.service.RequestLogService;
import com.internal.feature.telegram_alerts.config.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLogCleanupScheduler {

    private final RequestLogService requestLogService;
    private final AccountOnlineFinalRepository accountOnlineFinalRepository;
    private final AmlStatusRepository amlStatusRepository;
    private final TelegramService telegramService;

    private static final ZoneId ZONE_PP = ZoneId.of("Asia/Phnom_Penh");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${request.log.retention.days:30}")
    private int retentionDays;

    @Value("${aml.dashboard.url:}")
    private String amlDashboardUrl;

    // =====================================================
    // CLEANUP OLD LOGS - DAILY AT 2:00 AM
    // =====================================================
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Phnom_Penh")
    public void cleanupOldLogs() {
        log.info("Starting request log cleanup for logs older than {} days", retentionDays);
        try {
            int deleted = requestLogService.deleteOldLogs(retentionDays);
            log.info("Request log cleanup completed. Deleted {} records", deleted);
        } catch (Exception e) {
            log.error("Error during request log cleanup: {}", e.getMessage(), e);
        }
    }

    // =====================================================
    // DAILY ACCOUNT REPORT - EVERY DAY AT 8:00 AM
    // =====================================================
    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Phnom_Penh")
    public void sendDailyAccountReport() {
        sendDailyAccountReport(LocalDate.now(ZONE_PP).minusDays(1));
    }

    public void sendDailyAccountReport(LocalDate reportDate) {

        LocalDateTime startOfDay = reportDate.atStartOfDay();
        LocalDateTime endOfDay = reportDate.plusDays(1).atStartOfDay();

        log.info("Running daily account report for date: {}", reportDate);

        try {
            List<Object[]> results = accountOnlineFinalRepository
                    .countByGenderAndDateRange(startOfDay, endOfDay);

            long maleCount = 0;
            long femaleCount = 0;
            long otherCount = 0;

            for (Object[] row : results) {
                String gender = (String) row[0];
                long count = (Long) row[1];

                if ("MALE".equalsIgnoreCase(gender)) {
                    maleCount = count;
                } else if ("FEMALE".equalsIgnoreCase(gender)) {
                    femaleCount = count;
                } else {
                    otherCount += count;
                }
            }

            long totalCount = maleCount + femaleCount + otherCount;

            String formattedDate = reportDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            String generatedAt = LocalDateTime.now(ZONE_PP).format(FORMATTER);

            StringBuilder sb = new StringBuilder();
            sb.append("*DAILY ACCOUNT OPENING SUMMARY REPORT*\n")
                    .append("=================================\n")
                    .append("Report Date: *").append(formattedDate).append("*\n\n")
                    .append("Summary of Successfully Opened Accounts:\n\n")
                    .append("• Male: *").append(maleCount).append("*\n")
                    .append("• Female: *").append(femaleCount).append("*\n");

            if (otherCount > 0) {
                sb.append("• Other: *").append(otherCount).append("*\n");
            }

            sb.append("\nTotal Accounts Opened: *").append(totalCount).append("*\n\n")
                    .append("Report Generated At: ").append(generatedAt).append("\n")
                    .append("This is an automated notification from the Account Online System.");

            telegramService.sendMarkdownAccountOnlineMonitorMessage(sb.toString());

            log.info("Daily report sent. Total={}, Male={}, Female={}", totalCount, maleCount, femaleCount);

        } catch (Exception e) {
            log.error("Failed to send daily account report: {}", e.getMessage(), e);

            String errorMsg = "*DAILY ACCOUNT OPENING REPORT ERROR*\n"
                    + "=================================\n"
                    + "The system encountered an issue while generating the daily account opening report.\n"
                    + "Error Details: " + escapeMarkdown(e.getMessage()) + "\n"
                    + "Please review the application logs for further investigation.\n"
                    + "This is an automated notification from the Account Online System.";

            telegramService.sendMarkdownAccountOnlineMonitorMessage(errorMsg);
        }
    }

    // =====================================================
    // AML PENDING REPORT - EVERY DAY AT 8:01 AM
    // =====================================================
    @Scheduled(cron = "0 1 8 * * ?", zone = "Asia/Phnom_Penh")
    public void sendAmlPendingReport() {

        log.info("Running AML pending report");

        try {
            long pendingCount = amlStatusRepository.countByStatus(AmlStatusEnum.PENDING);

            String generatedAt = LocalDateTime.now(ZONE_PP).format(FORMATTER);

            StringBuilder sb = new StringBuilder();
            sb.append("*AML SCREENING STATUS REPORT*\n")
                    .append("=================================\n\n")
                    .append("Total Cases Pending Review: *").append(pendingCount).append("*\n\n");

            if (pendingCount > 0) {
                sb.append("There are currently *").append(pendingCount)
                        .append("* case(s) pending AML compliance review.\n")
                        .append("Kindly proceed with the necessary review and action at your earliest convenience.\n\n");

                if (amlDashboardUrl != null && !amlDashboardUrl.isEmpty()) {
                    sb.append("AML Dashboard: ")
                            .append(escapeMarkdown(amlDashboardUrl))
                            .append("\n\n");
                }
            } else {
                sb.append("There are no pending AML cases at this time.\n")
                        .append("All records are up to date.\n\n");
            }

            sb.append("Report Generated At: ").append(generatedAt).append("\n")
                    .append("This is an automated notification from the Account Online System.");

            telegramService.sendMarkdownAccountOnlineMonitorMessage(sb.toString());

            log.info("AML pending report sent. Pending={}", pendingCount);

        } catch (Exception e) {
            log.error("Failed to send AML pending report: {}", e.getMessage(), e);

            String errorMsg = "*AML SCREENING REPORT GENERATION ERROR*\n"
                    + "=================================\n"
                    + "The system encountered an issue while generating the AML screening status report.\n"
                    + "Error Details: " + escapeMarkdown(e.getMessage()) + "\n"
                    + "Please review the application logs for further investigation.\n"
                    + "This is an automated notification from the Account Online System.";

            telegramService.sendMarkdownAccountOnlineMonitorMessage(errorMsg);
        }
    }

    // =====================================================
    // HELPER
    // =====================================================
    private String escapeMarkdown(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }
}