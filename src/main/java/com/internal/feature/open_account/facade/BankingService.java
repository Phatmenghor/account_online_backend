package com.internal.feature.open_account.facade;

import com.internal.config.TestProperties;
import com.internal.exceptions.error.custom.NidValidationException;
import com.internal.exceptions.error.custom.ValidateServiceException;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.service.external.*;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankingService {

    private final ValidationService validationService;
    private final T24Service t24Service;
    private final MobileBankingService mobileBankingService;
    private final JdbcTemplate jdbcTemplate;
    private final TestProperties isTestMode;

    public static class TestConfig {
        public static final boolean SIMULATE_CAMDX_ERROR = false;
        public static final boolean SIMULATE_INTERNAL_ERROR = false;
    }

    public void testConnection() {
        log.info(">>> Step 1: TEST_CONNECTION");

        if (TestConfig.SIMULATE_INTERNAL_ERROR) {
            throw new ValidateServiceException("Simulated Internal T24 Connection Error (TEST_CONFIG)");
        }

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("Step 1 SUCCESS: Database connection is healthy");
        } catch (Exception e) {
            log.error("Step 1 FAILED: Database connection test failed", e);
            throw new RuntimeException("Unable to connect to the server. Please try again later. "
                    + "If the issue continues, contact our support team at 070 200 002 or 1800 200 888.");
        }
    }

    public Map<String, String> getCustomerInfo(String legalId) {
        log.info(">>> Step 2: GET_CUSTOMER_INFO");

        if (TestConfig.SIMULATE_CAMDX_ERROR) {
            throw new NidValidationException(500, "Simulated CAMDX/NID Validation Failure (TEST_CONFIG)");
        }

        Map<String, String> customerInfo = validationService.getCustomerInfo(legalId);
        log.info("Customer info retrieved: {}", customerInfo != null ? "Found" : "Not found");
        return customerInfo;
    }

    public void validateExistingAccounts(Map<String, String> customerInfo) {
        log.info(">>> Step 3: VALIDATE_EXISTING_ACCOUNTS (UAT profile)");
        validationService.validateExistingAccounts(customerInfo);
        log.info("Existing accounts validation passed");
    }

    public String createCustomer(CustomerRequest request) {
        Document resp = t24Service.createCustomer(request);
        return XmlParser.extractCif(resp);
    }

    public String getMnemonic(CustomerRequest request) {
        return XmlParser.extractMnemonic(t24Service.createCustomer(request));
    }

    public String createCustomerIfNeeded(CustomerRequest request, Map<String, String> customerInfo) {
        String existingCif = customerInfo.get("CIF");
        if (existingCif != null && !existingCif.isEmpty()) {
            log.info("Existing CIF found → Using existing customer");
            return existingCif;
        }

        Document resp = t24Service.createCustomer(request);
        return XmlParser.extractCif(resp);
    }

    public String createAccountIfNeeded(CustomerRequest request, Map<String, String> customerInfo, String cif,
            String currency) {
        if (isTestMode.isSkipCheckAccount()) {
            log.info("TEST MODE ENABLED — Skipping existing account check → creating new {} account", currency);
            return createAccount(request, cif, currency);
        }

        if (validationService.hasAccount(customerInfo, currency)) {
            log.info(">>> Step {}: CREATE_{}_ACCOUNT - SKIPPED (already exists)", currency.equals("KHR") ? 6 : 7,
                    currency);
            return null;
        }

        log.info(">>> Step {}: CREATE_{}_ACCOUNT", currency.equals("KHR") ? 6 : 7, currency);
        String account = createAccount(request, cif, currency);
        if (account != null) {
            log.info("Step {} SUCCESS: {} account created: {}", currency.equals("KHR") ? 6 : 7, currency, account);
        } else {
            log.warn("Step {} FAILED: {} account creation returned null", currency.equals("KHR") ? 6 : 7, currency);
        }
        return account;
    }

    private String createAccount(CustomerRequest request, String cif, String currency) {
        try {
            log.info("Creating {} account for CIF: {}", currency, cif);
            Document response = t24Service.createAccount(request, cif, currency);
            if (response == null) {
                log.warn("T24 returned null response for {} account", currency);
                return null;
            }
            String accountNumber = XmlParser.extractAccountNumber(response);
            log.info("{} account number extracted: {}", currency, accountNumber);
            return accountNumber;
        } catch (Exception e) {
            log.error("Error creating {} account: {}", currency, e.getMessage());
            return null;
        }
    }

    public void validateAtLeastOneAccountExists(Map<String, String> customerInfo, String khrAccount,
            String usdAccount) {
        log.info(">>> Step 8: VALIDATE_ACCOUNT_CREATION");
        if (khrAccount == null && usdAccount == null && !validationService.hasAccount(customerInfo, "KHR")
                && !validationService.hasAccount(customerInfo, "USD")) {
            throw new AccountCreationException(AppConstants.FAIL_CREATE_ANY_ACCOUNT);
        }
        log.info("Step 8 SUCCESS: At least one account exists");
    }

    public void activateMobileBanking(CustomerRequest request, String cif, String khrAccount, String usdAccount) {
        log.info(">>> Step 9: ACTIVATE_MOBILE_BANKING");
        try {
            mobileBankingService.activate(request, cif, khrAccount, usdAccount);
            log.info("Step 9 SUCCESS: Mobile banking activated");
        } catch (Exception e) {
            log.warn("Step 9 WARNING: Mobile banking activation failed (non-critical): {}", e.getMessage());
        }
    }
}
