package com.internal.feature.aml.event;

import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.logs_report.service.AccountOnlineOpenFinalService;
import com.internal.feature.telegram_alerts.service.serviceImpl.OpenAccountTelegramAlertServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AmlStatusChangedEventListener {

    private final AccountOnlineOpenFinalService accountOnlineOpenFinalService;
    private final OpenAccountTelegramAlertServiceImpl alertTelegramService;

    @EventListener
    public void handleAmlStatusChanged(AmlStatusChangedEvent event) {
        AmlStatusDto amlDto = event.getAmlStatusDto();
        String legalId = amlDto.getCustomerInfo() != null ? amlDto.getCustomerInfo().getLegalId() : "N/A";

        log.info("Processing post-update events for AML ID: {}, Legal ID: {}", amlDto.getId(), legalId);

        // Update the final log table
        try {
            accountOnlineOpenFinalService.updateFinalLogWithAml(amlDto);
        } catch (Exception e) {
            log.error("Failed to update final log with AML for Legal ID: {}. Error: {}", legalId, e.getMessage());
        }

        // Telegram notification
        try {
            alertTelegramService.sendTelegramAmlProcess(amlDto);
        } catch (Exception e) {
            log.error("Failed to send AML Telegram notification for Legal ID: {}. Error: {}", legalId, e.getMessage());
        }
    }
}
