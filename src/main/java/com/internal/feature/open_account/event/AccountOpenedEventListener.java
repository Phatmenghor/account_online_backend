package com.internal.feature.open_account.event;

import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.open_account.dto.OpenAccountContext;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.facade.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountOpenedEventListener {

    private final ReportingService reportingService;

    @EventListener
    public void handleAccountOpenedEvent(AccountOpenedEvent event) {
        OpenAccountContext context = event.getContext();
        log.info("Processing post-opening events for Legal ID: {}", context.getRequest().getLegalId());

        // Step 10: Save customer images
        CustomerImageUploadResponseDto imagePaths = reportingService.safeSaveCustomerImages(context.getRequest());

        // Step 11: Save success log
        CustomerResponse accInfo = CustomerResponse.builder()
                .cif(context.getCif())
                .khrAccount(context.getKhrAccount())
                .usdAccount(context.getUsdAccount())
                .mnemonic(context.getMnemonic())
                .build();

        reportingService.safeSaveSuccessLog(
                context.getRequest(),
                accInfo,
                context.getAmlResult(),
                imagePaths,
                context.getMbActivationCode()
        );

        // Step 12: Report log
        reportingService.safeReportLog(context.getRequest().getLegalId());

        log.info("Post-opening events completed for Legal ID: {}", context.getRequest().getLegalId());
    }
}
