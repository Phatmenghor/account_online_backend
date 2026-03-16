package com.internal.feature.open_account.service.impl;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.open_account.dto.OpenAccountContext;
import com.internal.feature.open_account.dto.request.CustomerCreationResult;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.event.AccountOpenedEvent;
import com.internal.feature.open_account.facade.BankingService;
import com.internal.feature.open_account.facade.ComplianceService;
import com.internal.feature.open_account.facade.ReportingService;
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.feature.telegram_alerts.service.MonitoringService;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountServiceImpl implements OpenAccountService {

    private final BankingService bankingService;
    private final ComplianceService complianceService;
    private final ReportingService reportingService;
    private final ApplicationEventPublisher eventPublisher;
    private final MonitoringService monitoringService;

    @Override
    @Transactional
    public CustomerResponse openAccount(CustomerRequest request) throws Exception {
        log.info("========== ACCOUNT OPENING STARTED ==========");
        log.info("Legal ID: {}", request.getLegalId());

        // Monitor: Account opening started
        monitoringService.logAccountOpeningStarted(
                request.getLegalId(),
                request.getNidImageName(),
                request.getSelfieImageName());

        OpenAccountContext context = OpenAccountContext.builder().request(request).build();
        String currentStep = "START";
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Test connection
            currentStep = AppConstants.TEST_CONNECTION;
            long stepStartTime = System.currentTimeMillis();
            bankingService.testConnection();
            long duration = System.currentTimeMillis() - stepStartTime;
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "TEST_CONNECTION", true, "Banking service connection verified (" + duration + "ms)");

            // Step 2: Customer matching
            currentStep = AppConstants.GET_CUSTOMER_INFO;
            stepStartTime = System.currentTimeMillis();
            context.setCustomerInfo(bankingService.getCustomerInfo(request.getLegalId()));
            duration = System.currentTimeMillis() - stepStartTime;
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "GET_CUSTOMER_INFO", true, "Customer found with CIF: " + (context.getCustomerInfo() != null ? context.getCustomerInfo().getCif() : "N/A"));

            // Step 3: Validation
            currentStep = AppConstants.VALIDATE_EXISTING_ACCOUNT;
            stepStartTime = System.currentTimeMillis();
            bankingService.validateExistingAccounts(context.getCustomerInfo());
            duration = System.currentTimeMillis() - stepStartTime;
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_EXISTING_ACCOUNT", true, "No existing accounts found");

            // Step 4: Process AML
            currentStep = AppConstants.PROCESS_AML;
            stepStartTime = System.currentTimeMillis();
            context.setAmlResult(complianceService.processAml(request));
            duration = System.currentTimeMillis() - stepStartTime;
            String amlStatus = context.getAmlResult() != null ? context.getAmlResult().getStatus().name() : "UNKNOWN";
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "PROCESS_AML", true, "AML Status: " + amlStatus + " (" + duration + "ms)");
            complianceService.sentMessageOnHighRisk(request, context.getAmlResult());

            // Step 5: Create customer
            currentStep = AppConstants.CREATE_CUSTOMER;
            stepStartTime = System.currentTimeMillis();
            CustomerCreationResult customerResult =
                    bankingService.createCustomerIfNeeded(request, context.getCustomerInfo());
            duration = System.currentTimeMillis() - stepStartTime;
            context.setCif(customerResult.getCif());
            context.setMnemonic(customerResult.getMnemonic());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_CUSTOMER", true, "CIF Created: " + customerResult.getCif());

            // Step 6: Create KHR Account
            currentStep = AppConstants.CREATE_KHR_ACCOUNT;
            stepStartTime = System.currentTimeMillis();
            context.setKhrAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_KHR));
            duration = System.currentTimeMillis() - stepStartTime;
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_KHR_ACCOUNT", true, "Account: " + context.getKhrAccount());

            // Step 7: Create USD Account
            currentStep = AppConstants.CREATE_USD_ACCOUNT;
            stepStartTime = System.currentTimeMillis();
            context.setUsdAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_USD));
            duration = System.currentTimeMillis() - stepStartTime;
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_USD_ACCOUNT", true, "Account: " + context.getUsdAccount());

            // Step 8: Final Validation
            currentStep = AppConstants.VALIDATE_ACCOUNT_CREATION;
            stepStartTime = System.currentTimeMillis();
            bankingService.validateAtLeastOneAccountExists(context.getCustomerInfo(),
                    context.getKhrAccount(),
                    context.getUsdAccount());
            duration = System.currentTimeMillis() - stepStartTime;
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_ACCOUNT_CREATION", true, "Accounts validated successfully");

            // Step 9: Activate mobile banking
            currentStep = AppConstants.ACTIVATE_MOBILE_BANKING;
            stepStartTime = System.currentTimeMillis();
            context.setMbActivationCode(
                    bankingService.activateMobileBanking(request, context.getCif(),
                            context.getKhrAccount(), context.getUsdAccount()));
            duration = System.currentTimeMillis() - stepStartTime;
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "ACTIVATE_MOBILE_BANKING", true, "Mobile Banking activated");

            // BUILD RESPONSE
            CustomerResponse accInfo = complianceService.buildCustomerAccInfo(
                    context.getCif(), context.getKhrAccount(), context.getUsdAccount(),
                    context.getMnemonic());

            // PUBLISH SUCCESS EVENT
            eventPublisher.publishEvent(new AccountOpenedEvent(this, context));

            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("========== ACCOUNT OPENING COMPLETED ==========");
            monitoringService.logAccountOpeningCompleted(
                    request.getLegalId(),
                    context.getCif(),
                    context.getKhrAccount(),
                    context.getUsdAccount(),
                    totalDuration);

            return accInfo;

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - startTime;
            log.error("========== ACCOUNT OPENING FAILED AT STEP: {} ==========", currentStep);

            // Monitor: Account opening failed
            monitoringService.logAccountOpeningFailed(
                    request.getLegalId(),
                    currentStep,
                    e.getMessage(),
                    e);

            // Save failure logs
            final String failureRemark = reportingService.buildFailureRemark(
                    currentStep,
                    context.getCif(),
                    context.getKhrAccount(),
                    context.getUsdAccount(),
                    context.getAmlResult());

            reportingService.saveFailureLogs(request, e, currentStep, failureRemark, false);

            throw e;
        }
    }
}