package com.internal.feature.open_account.service.impl;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.open_account.dto.OpenAccountContext;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.event.AccountOpenedEvent;
import com.internal.feature.open_account.facade.BankingService;
import com.internal.feature.open_account.facade.ComplianceService;
import com.internal.feature.open_account.facade.ReportingService;
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.internal.feature.logs_report.service.CustomerImageService;
import com.internal.feature.logs_report.dto.request.CustomerFileUploadRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountServiceImpl implements OpenAccountService {

        private final BankingService bankingService;
        private final ComplianceService complianceService;
        private final ReportingService reportingService;
        private final ApplicationEventPublisher eventPublisher;

        @Override
        @Transactional
        public CustomerResponse openAccount(CustomerRequest request) throws Exception {
                log.info("========== ACCOUNT OPENING STARTED ==========");
                log.info("Legal ID: {}", request.getLegalId());
                log.info("NID Image name: {}", request.getNidImageName());
                log.info("Selfie Image name: {}", request.getSelfieImageName());

                OpenAccountContext context = OpenAccountContext.builder().request(request).build();
                String currentStep = "START";

                try {
                        // Step 1: Test connection
                        currentStep = AppConstants.TEST_CONNECTION;
                        bankingService.testConnection();

                        // Step 2: Customer matching
                        currentStep = AppConstants.GET_CUSTOMER_INFO;
                        context.setCustomerInfo(bankingService.getCustomerInfo(request.getLegalId()));

                        // Step 3: Validation
                        currentStep = AppConstants.VALIDATE_EXISTING_ACCOUNT;
                        bankingService.validateExistingAccounts(context.getCustomerInfo());

                        // Step 4: Process AML — only Low risk (APPROVE) may continue; High risk or
                        // error stops here
                        currentStep = AppConstants.PROCESS_AML;
                        context.setAmlResult(complianceService.processAml(request));
                        complianceService.sentMessageOnHighRisk(request, context.getAmlResult());

                        // Step 5: Create customer
                        currentStep = AppConstants.CREATE_CUSTOMER;
                        context.setCif(bankingService.createCustomerIfNeeded(request, context.getCustomerInfo()));
                        context.setMnemonic(bankingService.getMnemonic(request));

                        // Step 6 & 7: Create Accounts (KHR & USD)
                        currentStep = AppConstants.CREATE_KHR_ACCOUNT;
                        context.setKhrAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                                        context.getCif(), AppConstants.CURRENCY_KHR));

                        currentStep = AppConstants.CREATE_USD_ACCOUNT;
                        context.setUsdAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                                        context.getCif(), AppConstants.CURRENCY_USD));

                        // Step 8: Final Validation
                        currentStep = AppConstants.VALIDATE_ACCOUNT_CREATION;
                        bankingService.validateAtLeastOneAccountExists(context.getCustomerInfo(),
                                        context.getKhrAccount(),
                                        context.getUsdAccount());

                        // Step 9: Activate mobile banking
                        currentStep = AppConstants.ACTIVATE_MOBILE_BANKING;
                        context.setMbActivationCode(
                                        bankingService.activateMobileBanking(request, context.getCif(),
                                                        context.getKhrAccount(), context.getUsdAccount()));

                        // BUILD RESPONSE
                        CustomerResponse accInfo = complianceService.buildCustomerAccInfo(
                                        context.getCif(), context.getKhrAccount(), context.getUsdAccount(),
                                        context.getMnemonic());

                        // PUBLISH SUCCESS EVENT (Steps 10, 11, 12 handled by EventListener)
                        eventPublisher.publishEvent(new AccountOpenedEvent(this, context));

                        log.info("========== ACCOUNT OPENING COMPLETED ==========");
                        return accInfo;

                } catch (Exception e) {
                        log.error("========== ACCOUNT OPENING FAILED AT STEP: {} ==========", currentStep);

                        String failureRemark = reportingService.buildFailureRemark(currentStep, context.getCif(),
                                        context.getKhrAccount(), context.getUsdAccount(), context.getAmlResult());

                        boolean skipTelegramAlert = false;

                        reportingService.saveFailureLogs(request, e, currentStep, failureRemark, skipTelegramAlert);

                        throw e;
                }
        }
}
