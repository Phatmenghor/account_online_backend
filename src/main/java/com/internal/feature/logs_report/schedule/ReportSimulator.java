package com.internal.feature.logs_report.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@ConditionalOnProperty(value = "simulator.report-logs", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ReportSimulator {

    private final RequestLogCleanupScheduler parent;

    @Scheduled(cron = "0 * * * * ?", zone = "Asia/Phnom_Penh")
    public void triggerDailyReport() {
        log.info("SIMULATION: Triggering Daily Account Report for TODAY");
        parent.sendDailyAccountReport(LocalDate.now(ZoneId.of("Asia/Phnom_Penh")));
    }

    @Scheduled(cron = "0 * * * * ?", zone = "Asia/Phnom_Penh")
    public void triggerAmlReport() {
        log.info("SIMULATION: Triggering AML Pending Report");
        parent.sendAmlPendingReport();
    }
}
