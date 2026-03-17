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

            // Step 2: Check existing complete account (recovery path for failed activation)
            currentStep = "CHECK_EXISTING_COMPLETE_ACCOUNT";
            var recoveryResult = bankingService.checkExistingCompleteAccountAndActivate(request);
            if (recoveryResult.isPresent()) {
                long totalDuration = System.currentTimeMillis() - startTime;
                log.info("========== ACCOUNT OPENING COMPLETED (RECOVERY) ==========");
                log.info("✓ Account already exists and is complete ({}ms)", totalDuration);
                // Build and return full response with account details
                var existingAccount = bankingService.getExistingAccountDetails(request.getLegalId());
                if (existingAccount.isPresent()) {
                    log.info("  • CIF: {} | Mnemonic: {}", existingAccount.get().getCif(), existingAccount.get().getMnemonic());
                    log.info("  • KHR Account: {}", existingAccount.get().getKhrAccount());
                    log.info("  • USD Account: {}", existingAccount.get().getUsdAccount());
                    return complianceService.buildCustomerAccInfo(
                            existingAccount.get().getCif(),
                            existingAccount.get().getKhrAccount(),
                            existingAccount.get().getUsdAccount(),
                            existingAccount.get().getMnemonic());
                }
            }

            // Step 3: Customer matching (continue normal flow)
            currentStep = AppConstants.GET_CUSTOMER_INFO;
            context.setCustomerInfo(bankingService.getCustomerInfo(request.getLegalId()));
            String customerCif = context.getCustomerInfo() != null ? context.getCustomerInfo().get("CIF") : "N/A";
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "GET_CUSTOMER_INFO", true, "Customer found with CIF: " + customerCif);

            // Step 4: Validation
            currentStep = AppConstants.VALIDATE_EXISTING_ACCOUNT;
            bankingService.validateExistingAccounts(context.getCustomerInfo());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_EXISTING_ACCOUNT", true, "No existing accounts found");

            // Step 5: Process AML
            currentStep = AppConstants.PROCESS_AML;
            context.setAmlResult(complianceService.processAml(request));
            String amlStatus = context.getAmlResult() != null ? context.getAmlResult().getStatus().name() : "UNKNOWN";
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "PROCESS_AML", true, "AML Status: " + amlStatus);
            complianceService.sentMessageOnHighRisk(request, context.getAmlResult());

            // Step 6: Create customer
            currentStep = AppConstants.CREATE_CUSTOMER;
            CustomerCreationResult customerResult =
                    bankingService.createCustomerIfNeeded(request, context.getCustomerInfo());
            context.setCif(customerResult.getCif());
            context.setMnemonic(customerResult.getMnemonic());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_CUSTOMER", true, "CIF Created: " + customerResult.getCif());

            // Step 7: Create KHR Account
            currentStep = AppConstants.CREATE_KHR_ACCOUNT;
            context.setKhrAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_KHR));
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_KHR_ACCOUNT", true, "Account: " + context.getKhrAccount());

            // Step 8: Create USD Account
            currentStep = AppConstants.CREATE_USD_ACCOUNT;
            context.setUsdAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_USD));
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_USD_ACCOUNT", true, "Account: " + context.getUsdAccount());

            // Step 9: Final Validation
            currentStep = AppConstants.VALIDATE_ACCOUNT_CREATION;
            bankingService.validateAllRequiredAccountsCreated(context.getCif(),
                    context.getKhrAccount(),
                    context.getUsdAccount());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_ACCOUNT_CREATION", true, "Accounts validated successfully");

            // Step 10: Activate mobile banking
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
            log.info("✓ Successfully created accounts in {}ms", totalDuration);
            log.info("  • Legal ID: {}", request.getLegalId());
            log.info("  • CIF: {} | Mnemonic: {}", context.getCif(), context.getMnemonic());
            log.info("  • KHR Account: {}", context.getKhrAccount());
            log.info("  • USD Account: {}", context.getUsdAccount());
            log.info("  • Mobile Banking Code: {}", context.getMbActivationCode() != null ? "✓ Activated" : "N/A");

            monitoringService.logAccountOpeningCompleted(
                    request.getLegalId(),
                    context.getCif(),
                    context.getKhrAccount(),
                    context.getUsdAccount(),
                    totalDuration);

            return accInfo;

        } catch (Exception e) {
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