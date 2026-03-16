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
            bankingService.testConnection();
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "TEST_CONNECTION", true, "Banking service connection verified");

            // Step 2: Customer matching
            currentStep = AppConstants.GET_CUSTOMER_INFO;
            context.setCustomerInfo(bankingService.getCustomerInfo(request.getLegalId()));
            String customerCif = context.getCustomerInfo() != null ? context.getCustomerInfo().get("CIF") : "N/A";
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "GET_CUSTOMER_INFO", true, "Customer found with CIF: " + customerCif);

            // Step 2.5: Check existing complete account (recovery path for failed activation)
            currentStep = "CHECK_EXISTING_COMPLETE_ACCOUNT";
            var recoveryResult = bankingService.checkExistingCompleteAccountAndActivate(request);
            if (recoveryResult.isPresent()) {
                String mbCode = recoveryResult.get();
                log.info("========== ACCOUNT OPENING COMPLETED (RECOVERY PATH) ==========");
                monitoringService.logAccountOpeningCompleted(
                        request.getLegalId(),
                        context.getCustomerInfo() != null ? context.getCustomerInfo().get("CIF") : "N/A",
                        context.getCustomerInfo() != null ? context.getCustomerInfo().get("KHR") : "N/A",
                        context.getCustomerInfo() != null ? context.getCustomerInfo().get("USD") : "N/A",
                        System.currentTimeMillis() - startTime);
                // Return recovery result
                return complianceService.buildCustomerAccInfo(
                        context.getCustomerInfo() != null ? context.getCustomerInfo().get("CIF") : null,
                        context.getCustomerInfo() != null ? context.getCustomerInfo().get("KHR") : null,
                        context.getCustomerInfo() != null ? context.getCustomerInfo().get("USD") : null,
                        context.getCustomerInfo() != null ? context.getCustomerInfo().get("MNEMONIC") : null);
            }

            // Step 3: Validation
            currentStep = AppConstants.VALIDATE_EXISTING_ACCOUNT;
            bankingService.validateExistingAccounts(context.getCustomerInfo());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_EXISTING_ACCOUNT", true, "No existing accounts found");

            // Step 4: Process AML
            currentStep = AppConstants.PROCESS_AML;
            context.setAmlResult(complianceService.processAml(request));
            String amlStatus = context.getAmlResult() != null ? context.getAmlResult().getStatus().name() : "UNKNOWN";
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "PROCESS_AML", true, "AML Status: " + amlStatus);
            complianceService.sentMessageOnHighRisk(request, context.getAmlResult());

            // Step 5: Create customer
            currentStep = AppConstants.CREATE_CUSTOMER;
            CustomerCreationResult customerResult =
                    bankingService.createCustomerIfNeeded(request, context.getCustomerInfo());
            context.setCif(customerResult.getCif());
            context.setMnemonic(customerResult.getMnemonic());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_CUSTOMER", true, "CIF Created: " + customerResult.getCif());

            // Step 6: Create KHR Account
            currentStep = AppConstants.CREATE_KHR_ACCOUNT;
            context.setKhrAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_KHR));
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_KHR_ACCOUNT", true, "Account: " + context.getKhrAccount());

            // Step 7: Create USD Account
            currentStep = AppConstants.CREATE_USD_ACCOUNT;
            context.setUsdAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_USD));
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_USD_ACCOUNT", true, "Account: " + context.getUsdAccount());

            // Step 8: Final Validation
            currentStep = AppConstants.VALIDATE_ACCOUNT_CREATION;
            bankingService.validateAllRequiredAccountsCreated(context.getCustomerInfo(),
                    context.getKhrAccount(),
                    context.getUsdAccount());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_ACCOUNT_CREATION", true, "Accounts validated successfully");

            // Step 9: Activate mobile banking
            currentStep = AppConstants.ACTIVATE_MOBILE_BANKING;
            context.setMbActivationCode(
                    bankingService.activateMobileBanking(request, context.getCif(),
                            context.getKhrAccount(), context.getUsdAccount()));
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