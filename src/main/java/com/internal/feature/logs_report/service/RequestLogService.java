package com.internal.feature.logs_report.service;

import com.internal.feature.logs_report.model.RequestLog;
import com.internal.feature.logs_report.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;

    /**
     * Save request log to database asynchronously
     * @param requestLog The request log to save
     */
    @Async
    @Transactional
    public void saveLog(RequestLog requestLog) {
        try {
            requestLogRepository.save(requestLog);
            log.debug("Saved request log: UUID={}, Endpoint={} {}, Status={}", 
                    requestLog.getId(), 
                    requestLog.getMethod(),
                    requestLog.getEndpoint(),
                    requestLog.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to save request log: {}", e.getMessage());
        }
    }

    /**
     * Delete logs older than specified days
     * @param days Number of days to keep
     * @return Number of deleted records
     */
    @Transactional
    public int deleteOldLogs(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int deleted = requestLogRepository.deleteLogsOlderThan(cutoffDate);
        log.info("Deleted {} old request logs older than {} days", deleted, days);
        return deleted;
    }
}
