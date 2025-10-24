package com.internal.feature.open_account.service.impl;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.service.*;
import com.internal.feature.open_account.service.external.MobileBankingService;
import com.internal.feature.open_account.service.external.T24Service;
import com.internal.feature.open_account.service.external.ValidationService;
import com.internal.feature.open_account.service.external.XmlParser;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountServiceImpl implements OpenAccountService {

    private final ValidationService validationService;
    private final T24Service t24Service;
    private final MobileBankingService mobileBankingService;
    private final AccountOnlineReportLogService reportLogService;

    @Override
    @Transactional
    public CustomerResponse openAccount(CustomerRequest request) {
        log.info("Processing account opening for Legal ID: {}", request.getLegalId());

        String currentStep = "START";
        String cif = null;
        String mnemonic = null;
        String khrAccount = null;
        String usdAccount = null;

        try {
            // Step 1: Check database connections
            currentStep = "CHECK_DATABASE_CONNECTIONS";
            validationService.checkDatabaseConnections();
            log.info("Database connections verified for Legal ID: {}", request.getLegalId());


            // Step 2: Get customer info
            currentStep = "GET_CUSTOMER_INFO";
            Map<String, String> customerInfo = validationService.getCustomerInfo(request.getLegalId());
            log.info("Customer info retrieved for Legal ID: {}", request.getLegalId());

            // Step 3: Validate customer rating
            currentStep = "VALIDATE_CUSTOMER_RATING";
            validationService.validateCustomerRating(customerInfo);
            log.info("Customer rating validated for Legal ID: {}", request.getLegalId());

            // Step 4: Validate existing accounts
            currentStep = "VALIDATE_EXISTING_ACCOUNTS";
            validationService.validateExistingAccounts(customerInfo);
            log.info("Existing accounts validated for Legal ID: {}", request.getLegalId());

            // Step 5: Create customer if needed
            currentStep = "CREATE_CUSTOMER";
            cif = customerInfo.get("CIF");

            if (cif == null || cif.isEmpty()) {
                Map<String, String> customerData = createCustomer(request);
                cif = customerData.get("cif");
                mnemonic = customerData.get("mnemonic");
                log.info("New customer created - CIF: {} for Legal ID: {}", cif, request.getLegalId());
            } else {
                log.info("Using existing CIF: {} for Legal ID: {}", cif, request.getLegalId());
            }

            // Step 6: Create KHR account
            if (!validationService.hasAccount(customerInfo, "KHR")) {
                currentStep = "CREATE_KHR_ACCOUNT";
                khrAccount = createAccount(request, cif, "KHR");
                if (khrAccount != null) {
                    log.info("KHR account created: {} for CIF: {}", khrAccount, cif);
                }
            } else {
                log.info("KHR account already exists for CIF: {}", cif);
            }

            // Step 7: Create USD account
            if (!validationService.hasAccount(customerInfo, "USD")) {
                currentStep = "CREATE_USD_ACCOUNT";
                usdAccount = createAccount(request, cif, "USD");
                if (usdAccount != null) {
                    log.info("USD account created: {} for CIF: {}", usdAccount, cif);
                }
            } else {
                log.info("USD account already exists for CIF: {}", cif);
            }

            // Step 8: Validate at least one account created
            currentStep = "VALIDATE_ACCOUNT_CREATION";
            if (khrAccount == null && usdAccount == null &&
                    !validationService.hasAccount(customerInfo, "KHR") &&
                    !validationService.hasAccount(customerInfo, "USD")) {
                throw new AccountCreationException(AppConstants.FAIL_CREATE_ANY_ACCOUNT);
            }

            // Step 8: Validate at least one account created
            currentStep = "VALIDATE_ACCOUNT_CREATION";
            if (khrAccount == null && usdAccount == null &&
                    !validationService.hasAccount(customerInfo, "KHR") &&
                    !validationService.hasAccount(customerInfo, "USD")) {
                throw new AccountCreationException(AppConstants.FAIL_CREATE_ANY_ACCOUNT);
            }

            // Step 9: Activate mobile banking (non-blocking)
            currentStep = "ACTIVATE_MOBILE_BANKING";
            try {
                mobileBankingService.activate(request, cif, khrAccount, usdAccount);
                log.info("Mobile banking activation completed for CIF: {}", cif);
            } catch (Exception e) {
                log.error("Mobile banking activation failed (non-critical) for CIF {}: {}", cif, e.getMessage());
            }

            // Step 10: Success - Log the successful completion
            currentStep = "COMPLETED";
            String successRemark = buildSuccessRemark(cif, khrAccount, usdAccount, mnemonic);
            reportLogService.createAccountOpeningLog(
                    request.getLegalId(),
                    OpenAccStatusEnum.SUCCESS,
                    successRemark,
                    null
            );
            log.info("Account opening completed successfully for Legal ID: {}", request.getLegalId());


            // Step 11: Return success response
            return CustomerResponse.builder()
                    .cif(cif)
                    .khrAccount(khrAccount)
                    .usdAccount(usdAccount)
                    .mnemonic(mnemonic)
                    .build();

        } catch (Exception e) {
            // Log the failure with current step information
            String failureRemark = buildFailureRemark(currentStep, cif, khrAccount, usdAccount);
            reportLogService.createAccountOpeningLog(
                    request.getLegalId(),
                    OpenAccStatusEnum.FAILURE,
                    failureRemark,
                    e
            );

            log.error("Account opening failed at step {} for Legal ID: {} - Error: {}",
                    currentStep, request.getLegalId(), e.getMessage());
            throw e;
        }
    }

    private Map<String, String> createCustomer(CustomerRequest request) {
        log.info("Creating new customer for Legal ID: {}", request.getLegalId());

        Document response = t24Service.createCustomer(request);
        if (response == null) {
            throw new AccountCreationException("T24 returned null response for customer creation");
        }

        // Extract CIF
        String cif = XmlParser.extractCif(response);
        if (cif == null || cif.isEmpty()) {
            throw new AccountCreationException("No CIF returned from T24");
        }

        // Extract MNEMONIC
        String mnemonic = XmlParser.extractMnemonic(response);

        log.info("Customer created - CIF: {}, MNEMONIC: {}", cif, mnemonic);

        // Use HashMap instead of Map.of()
        Map<String, String> result = new HashMap<>();
        result.put("cif", cif);
        result.put("mnemonic", mnemonic != null ? mnemonic : "");
        return result;
    }

    private String buildSuccessRemark(String cif, String khrAccount, String usdAccount, String mnemonic) {
        StringBuilder remark = new StringBuilder("Account opening completed successfully");

        if (cif != null) {
            remark.append(" | CIF: ").append(cif);
        }
        if (khrAccount != null) {
            remark.append(" | KHR Account: ").append(khrAccount);
        }
        if (usdAccount != null) {
            remark.append(" | USD Account: ").append(usdAccount);
        }
        if (mnemonic != null && !mnemonic.isEmpty()) {
            remark.append(" | Mnemonic: ").append(mnemonic);
        }

        return remark.toString();
    }

    private String buildFailureRemark(String failedStep, String cif, String khrAccount, String usdAccount) {
        StringBuilder remark = new StringBuilder("Account opening failed at: ").append(failedStep);

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

    private String createAccount(CustomerRequest request, String cif, String currency) {
        try {
            log.info("Creating {} account for CIF: {}", currency, cif);

            Document response = t24Service.createAccount(request, cif, currency);
            if (response == null) {
                log.warn("Failed to create {} account - null response", currency);
                return null;
            }

            String accountNumber = XmlParser.extractAccountNumber(response);
            log.info("{} account created: {}", currency, accountNumber);
            return accountNumber;

        } catch (Exception e) {
            log.error("Error creating {} account: {}", currency, e.getMessage());
            return null;
        }
    }
}