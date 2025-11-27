package com.internal.feature.logs_report.schedule;

import com.internal.feature.logs_report.service.RequestLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to automatically clean up old request logs.
 * Runs daily to keep database size manageable.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLogCleanupScheduler {

    private final RequestLogService requestLogService;

    @Value("${request.log.retention.days:14}")
    private int retentionDays;

    /**
     * Clean up old logs daily at 2 AM
     * Deletes logs older than (30 days)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs() {
        log.info("Starting request log cleanup for logs older than {} days", retentionDays);
        try {
            int deleted = requestLogService.deleteOldLogs(retentionDays);
            log.info("Request log cleanup completed. Deleted {} records", deleted);
        } catch (Exception e) {
            log.error("Error during request log cleanup: {}", e.getMessage(), e);
        }
    }
}
