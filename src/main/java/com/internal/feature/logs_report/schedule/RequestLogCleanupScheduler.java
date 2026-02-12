package com.internal.feature.logs_report.schedule;

import com.internal.feature.logs_report.repository.AccountOnlineFinalRepository;
import com.internal.feature.aml.repository.AmlStatusRepository;
import com.internal.feature.logs_report.service.RequestLogService;
import com.internal.feature.telegram_alerts.config.TelegramService;
import com.internal.enumation.AmlStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        LocalDate yesterday = LocalDate.now(ZONE_PP).minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.plusDays(1).atStartOfDay();

        log.info("Running daily account report for date: {}", yesterday);

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

            String reportDate = yesterday.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            String generatedAt = LocalDateTime.now(ZONE_PP).format(FORMATTER);

            StringBuilder sb = new StringBuilder();
            sb.append("*DAILY ACCOUNT OPENING REPORT*\n")
                    .append("--------------------\n")
                    .append("Report Date: *").append(reportDate).append("*\n\n")
                    .append("*Successfully Opened Accounts*\n\n")
                    .append("Male: *").append(maleCount).append("*\n")
                    .append("Female: *").append(femaleCount).append("*\n");

            if (otherCount > 0) {
                sb.append("Other: *").append(otherCount).append("*\n");
            }

            sb.append("\nTotal: *").append(totalCount).append("*\n")
                    .append("--------------------\n")
                    .append("Generated: ").append(generatedAt).append("\n")
                    .append("Auto Report \\- Account Online System");

            telegramService.sendMarkdownAccountOnlineMonitorMessage(sb.toString());

            log.info("Daily report sent. Total={}, Male={}, Female={}", totalCount, maleCount, femaleCount);

        } catch (Exception e) {
            log.error("Failed to send daily account report: {}", e.getMessage(), e);

            String errorMsg = "*DAILY REPORT ERROR*\n"
                    + "--------------------\n"
                    + "Failed to generate daily account report\\.\n"
                    + "Error: " + escapeMarkdown(e.getMessage()) + "\n"
                    + "Please check logs\\.";

            telegramService.sendMarkdownAccountOnlineMonitorMessage(errorMsg);
        }
    }

    // =====================================================
    // AML PENDING REPORT - EVERY DAY AT 8:00 AM
    // =====================================================
    @Scheduled(cron = "0 1 8 * * ?", zone = "Asia/Phnom_Penh")
    public void sendAmlPendingReport() {

        log.info("Running AML pending report");

        try {
            long pendingCount = amlStatusRepository.countByStatus(AmlStatusEnum.PENDING);

            String generatedAt = LocalDateTime.now(ZONE_PP).format(FORMATTER);

            StringBuilder sb = new StringBuilder();
            sb.append("*AML SCREENING PENDING REPORT*\n")
                    .append("--------------------\n\n")
                    .append("Total Pending: *").append(pendingCount).append("*\n\n");

            if (pendingCount > 0) {
                sb.append("There are *").append(pendingCount)
                        .append("* customer(s) awaiting AML review\\.\n")
                        .append("Please take action as soon as possible\\.\n\n");

                if (amlDashboardUrl != null && !amlDashboardUrl.isEmpty()) {
                    sb.append("Dashboard: ").append(escapeMarkdown(amlDashboardUrl)).append("\n");
                }
            } else {
                sb.append("No pending AML cases\\. All clear\\.\n");
            }

            sb.append("--------------------\n")
                    .append("Generated: ").append(generatedAt).append("\n")
                    .append("Auto Report \\- Account Online System");

            telegramService.sendMarkdownAccountOnlineMonitorMessage(sb.toString());

            log.info("AML pending report sent. Pending={}", pendingCount);

        } catch (Exception e) {
            log.error("Failed to send AML pending report: {}", e.getMessage(), e);

            String errorMsg = "*AML REPORT ERROR*\n"
                    + "--------------------\n"
                    + "Failed to generate AML pending report\\.\n"
                    + "Error: " + escapeMarkdown(e.getMessage()) + "\n"
                    + "Please check logs\\.";

            telegramService.sendMarkdownAccountOnlineMonitorMessage(errorMsg);
        }
    }

    // =====================================================
    // HELPER
    // =====================================================
    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }
}