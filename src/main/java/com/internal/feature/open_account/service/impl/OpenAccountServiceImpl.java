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
            log.info(">>> Step 1: TEST_CONNECTION");
            currentStep = AppConstants.TEST_CONNECTION;
            bankingService.testConnection();
            log.info("Step 1 ✓ SUCCESS: Database connection is healthy");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "TEST_CONNECTION", true, "Banking service connection verified");

            // Step 2: Check existing complete account (recovery path for failed activation)
            log.info(">>> Step 2: CHECK_EXISTING_COMPLETE_ACCOUNT");
            currentStep = "CHECK_EXISTING_COMPLETE_ACCOUNT";
            var recoveryResult = bankingService.checkExistingCompleteAccountAndActivate(request);
            if (recoveryResult.isPresent()) {
                long totalDuration = System.currentTimeMillis() - startTime;
                log.info("Step 2 ✓ SUCCESS: Account recovery completed");
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
            log.info("Step 2 ✓ INFO: No existing complete account found - continuing normal flow");

            // Step 3: Customer matching (continue normal flow)
            log.info(">>> Step 3: GET_CUSTOMER_INFO");
            currentStep = AppConstants.GET_CUSTOMER_INFO;
            context.setCustomerInfo(bankingService.getCustomerInfo(request.getLegalId()));
            String customerCif = context.getCustomerInfo() != null ? context.getCustomerInfo().get("CIF") : "N/A";
            log.info("Step 3 ✓ SUCCESS: Customer info retrieved");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "GET_CUSTOMER_INFO", true, "Customer found with CIF: " + customerCif);

            // Step 4: Validation
            log.info(">>> Step 4: VALIDATE_EXISTING_ACCOUNTS");
            currentStep = AppConstants.VALIDATE_EXISTING_ACCOUNT;
            bankingService.validateExistingAccounts(context.getCustomerInfo());
            log.info("Step 4 ✓ SUCCESS: No existing accounts found");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_EXISTING_ACCOUNT", true, "No existing accounts found");

            // Step 5: Process AML
            log.info(">>> Step 5: PROCESS_AML");
            currentStep = AppConstants.PROCESS_AML;
            context.setAmlResult(complianceService.processAml(request));
            String amlStatus = context.getAmlResult() != null ? context.getAmlResult().getStatus().name() : "UNKNOWN";
            log.info("Step 5 ✓ SUCCESS: AML processing completed | Status: {}", amlStatus);
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "PROCESS_AML", true, "AML Status: " + amlStatus);
            complianceService.sentMessageOnHighRisk(request, context.getAmlResult());

            // Step 6: Create customer
            log.info(">>> Step 6: CREATE_CUSTOMER");
            currentStep = AppConstants.CREATE_CUSTOMER;
            CustomerCreationResult customerResult =
                    bankingService.createCustomerIfNeeded(request, context.getCustomerInfo());
            context.setCif(customerResult.getCif());
            context.setMnemonic(customerResult.getMnemonic());
            log.info("Step 6 ✓ SUCCESS: Customer created | CIF: {}, Mnemonic: {}", context.getCif(), context.getMnemonic());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_CUSTOMER", true, "CIF Created: " + customerResult.getCif());

            // Step 7: Create KHR Account
            log.info(">>> Step 7: CREATE_KHR_ACCOUNT");
            currentStep = AppConstants.CREATE_KHR_ACCOUNT;
            context.setKhrAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_KHR));
            log.info("Step 7 ✓ SUCCESS: KHR account created | Account: {}", context.getKhrAccount());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_KHR_ACCOUNT", true, "Account: " + context.getKhrAccount());

            // Step 8: Create USD Account
            log.info(">>> Step 8: CREATE_USD_ACCOUNT");
            currentStep = AppConstants.CREATE_USD_ACCOUNT;
            context.setUsdAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_USD));
            log.info("Step 8 ✓ SUCCESS: USD account created | Account: {}", context.getUsdAccount());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_USD_ACCOUNT", true, "Account: " + context.getUsdAccount());

            // Step 9: Final Validation
            log.info(">>> Step 9: VALIDATE_ACCOUNT_CREATION");
            currentStep = AppConstants.VALIDATE_ACCOUNT_CREATION;
            bankingService.validateAllRequiredAccountsCreated(context.getCif(),
                    context.getKhrAccount(),
                    context.getUsdAccount());
            log.info("Step 9 ✓ SUCCESS: All accounts validated");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_ACCOUNT_CREATION", true, "Accounts validated successfully");

            // Step 10: Activate mobile banking
            log.info(">>> Step 10: ACTIVATE_MOBILE_BANKING");
            currentStep = AppConstants.ACTIVATE_MOBILE_BANKING;
            context.setMbActivationCode(
                    bankingService.activateMobileBanking(request, context.getCif(),
                            context.getKhrAccount(), context.getUsdAccount()));
            log.info("Step 10 ✓ SUCCESS: Mobile banking activated");
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