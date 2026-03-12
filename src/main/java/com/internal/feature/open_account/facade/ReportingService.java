package com.internal.feature.open_account.facade;

import com.internal.enumation.AmlStatusEnum;
import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.service.AccountOnlineOpenFinalService;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.telegram_alerts.service.serviceImpl.OpenAccountTelegramAlertServiceImpl;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportingService {

    private final AccountOnlineReportLogService reportLogService;
    private final AccountOnlineOpenFinalService accountOnlineOpenSuccessService;
    private final OpenAccountTelegramAlertServiceImpl alertTelegramService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailureLogs(CustomerRequest request, Exception e, String currentStep, String failureRemark,
                                boolean skipTelegramAlert) {
        OpenAccStatusEnum status = OpenAccStatusEnum.FAILURE;
        if (currentStep.equals(AppConstants.PROCESS_AML)) {
            status = OpenAccStatusEnum.AML;
        }

        reportLogService.createAccountOpeningLog(request.getLegalId(), status, failureRemark, e);

        if (skipTelegramAlert) {
            log.info("Skipping generic Telegram alert for step {} as strictly handled by specific logic.", currentStep);
            return;
        }

        StringBuilder remarkBuilder = new StringBuilder(failureRemark);
        remarkBuilder.append(" | Error: ").append(e.getMessage());

        boolean isAccountExistsError = e.getMessage() != null && e.getMessage().contains("Account already exists");

        if (isMonitorAlertStep(currentStep) && !isAccountExistsError) {
            if (!AppConstants.PROCESS_AML.equals(currentStep)) {
                alertTelegramService.sendTelegramAccountOnlineError(request.getLegalId(), status, remarkBuilder);
            }
        } else {
            alertTelegramService.sendTelegramInternalError(request.getLegalId(), status, remarkBuilder);
        }
    }

    private boolean isMonitorAlertStep(String step) {
        return AppConstants.PROCESS_AML.equals(step) || AppConstants.GET_CUSTOMER_INFO.equals(step);
    }

    public String buildFailureRemark(String failedStep, String cif, String khrAccount, String usdAccount,
                                     AmlStatusDto amlProcessResult) {
        StringBuilder remark = new StringBuilder("Account opening failed at: ").append(failedStep);

        if (amlProcessResult != null && amlProcessResult.getStatus() != null
                && !amlProcessResult.getStatus().equals(AmlStatusEnum.APPROVE)) {
            remark.append(" | AML Status: ").append(amlProcessResult.getStatus());
        }

        if (cif != null) {
            remark.append(" | CIF: ").append(cif);
        }
        if (khrAccount != null) {
            remark.append(" | KHR Account: ").append(khrAccount);
        }
        if (usdAccount != null) {
            remark.append(" | USD Account: ").append(usdAccount);
        }

        return remark.toString();
    }

    public CustomerImageUploadResponseDto safeSaveCustomerImages(CustomerRequest request) {
        log.info(">>> Step 10: SAVE_CUSTOMER_IMAGES (using pre-uploaded filenames)");
        try {
            CustomerImageUploadResponseDto imagePaths = CustomerImageUploadResponseDto.builder()
                    .nidImagePath(request.getNidImageName())
                    .selfieImagePath(request.getSelfieImageName())
                    .build();
            log.info("Step 10 SUCCESS: Image filenames resolved - NID: {}, Selfie: {}",
                    request.getNidImageName(), request.getSelfieImageName());
            return imagePaths;
        } catch (Exception e) {
            log.warn("Step 10 WARNING: Failed to resolve image filenames (non-critical): {}", e.getMessage());
            return null;
        }
    }

    public void safeSaveSuccessLog(CustomerRequest request, CustomerResponse accountInfo,
                                   AmlStatusDto amlStatusResponseDto, CustomerImageUploadResponseDto imagePaths,
                                   String mbActivationCode) {
        log.info(">>> Step 11: SAVE_SUCCESS_LOG");
        try {
            accountOnlineOpenSuccessService.saveFinalLog(
                    request,
                    accountInfo,
                    amlStatusResponseDto,
                    imagePaths,
                    mbActivationCode);
            log.info("Step 11 SUCCESS: Success log saved");
        } catch (Exception e) {
            log.warn("Step 11 WARNING: Failed to save success log (non-critical): {}", e.getMessage());
        }
    }

    public void safeReportLog(String legalId) {
        log.info(">>> Step 12: SAVE_REPORT_LOG");
        try {
            reportLogService.saveLogReport(legalId, OpenAccStatusEnum.SUCCESS, "Open account online Successfully");
            log.info("Step 12 SUCCESS: Report log saved");
        } catch (Exception e) {
            log.warn("Step 12 WARNING: Failed to save report log (non-critical): {}", e.getMessage());
        }
    }
}