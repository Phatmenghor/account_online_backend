package com.internal.feature.open_account.facade;

import com.internal.config.TestProperties;
import com.internal.exceptions.error.custom.NidValidationException;
import com.internal.exceptions.error.custom.ValidateServiceException;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.open_account.dto.request.CustomerCreationResult;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.service.external.*;
import com.internal.utils.constants.AppConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${simulator.banking.camdx-error:false}")
    private boolean simulateCamdxError;

    @Value("${simulator.banking.internal-error:false}")
    private boolean simulateInternalError;

    // ─── Step 1: Test Connection ──────────────────────────────────────────────
    public void testConnection() {
        log.info(">>> Step 1: TEST_CONNECTION");

        if (simulateInternalError) {
            throw new ValidateServiceException("Simulated Internal T24 Connection Error (SIMULATION)");
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

    // ─── Step 2: Get Customer Info ────────────────────────────────────────────
    public Map<String, String> getCustomerInfo(String legalId) {
        log.info(">>> Step 2: GET_CUSTOMER_INFO");

        if (simulateCamdxError) {
            throw new NidValidationException(500, "Simulated CAMDX/NID Validation Failure (SIMULATION)");
        }

        Map<String, String> customerInfo = validationService.getCustomerInfo(legalId);
        log.info("Customer info retrieved: {}", customerInfo != null ? "Found" : "Not found");
        return customerInfo;
    }

    // ─── Step 3: Validate Existing Accounts ───────────────────────────────────
    public void validateExistingAccounts(Map<String, String> customerInfo) {
        log.info(">>> Step 3: VALIDATE_EXISTING_ACCOUNTS");
        validationService.validateExistingAccounts(customerInfo);
        log.info("Existing accounts validation passed");
    }

    // ─── Step 5: Create Customer ──────────────────────────────────────────────
    /**
     * Creates a new customer in T24 if no existing CIF is found.
     * Extracts both CIF and MNEMONIC from a SINGLE t24Service.createCustomer() call.
     * For existing customers, both CIF and MNEMONIC are taken directly from customerInfo.
     */
    public CustomerCreationResult createCustomerIfNeeded(CustomerRequest request, Map<String, String> customerInfo) {
        String existingCif = customerInfo.get("CIF");

        if (existingCif != null && !existingCif.isEmpty()) {
            log.info("Existing CIF found → Using existing customer");
            String existingMnemonic = customerInfo.get("MNEMONIC");
            log.info("Existing MNEMONIC retrieved from customerInfo: {}", existingMnemonic);
            return new CustomerCreationResult(existingCif, existingMnemonic);
        }

        // Single T24 call — extract both CIF and MNEMONIC from one response
        Document resp = t24Service.createCustomer(request);
        String cif = XmlParser.extractCif(resp);
        String mnemonic = XmlParser.extractMnemonic(resp);
        log.info("New customer created → CIF: {}, MNEMONIC: {}", cif, mnemonic);

        return new CustomerCreationResult(cif, mnemonic);
    }

    // ─── Steps 6 & 7: Create Accounts ────────────────────────────────────────
    public String createAccountIfNeeded(CustomerRequest request, Map<String, String> customerInfo, String cif,
                                        String currency) {
        // Add 3-second delay before creating account
        delayMs(3000);

        if (isTestMode.isSkipCheckAccount()) {
            log.info("TEST MODE ENABLED — Skipping existing account check → creating new {} account", currency);
            return createAccount(request, cif, currency);
        }

        if (validationService.hasAccount(customerInfo, currency)) {
            log.info(">>> Step {}: CREATE_{}_ACCOUNT - SKIPPED (already exists)",
                    AppConstants.CURRENCY_KHR.equals(currency) ? 6 : 7, currency);
            return null;
        }

        log.info(">>> Step {}: CREATE_{}_ACCOUNT", AppConstants.CURRENCY_KHR.equals(currency) ? 6 : 7, currency);
        String account = createAccountWithRetry(request, customerInfo, cif, currency);
        if (account != null) {
            log.info("Step {} SUCCESS: {} account created: {}",
                    AppConstants.CURRENCY_KHR.equals(currency) ? 6 : 7, currency, account);
        } else {
            log.warn("Step {} FAILED: {} account creation returned null",
                    AppConstants.CURRENCY_KHR.equals(currency) ? 6 : 7, currency);
        }
        return account;
    }

    private String createAccountWithRetry(CustomerRequest request, Map<String, String> customerInfo, String cif, String currency) {
        int maxRetries = 3;
        int retryDelay = 3000; // 3 seconds

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return createAccount(request, cif, currency);
            } catch (Exception e) {
                log.warn("Attempt {} failed to create {} account: {}", attempt, currency, e.getMessage());

                if (attempt < maxRetries) {
                    // Verify customer info before retry
                    try {
                        Map<String, String> freshCustomerInfo = getCustomerInfo(request.getLegalId());
                        log.info("Customer info verified before retry attempt {}", attempt + 1);
                    } catch (Exception verifyError) {
                        log.error("Failed to verify customer info before retry: {}", verifyError.getMessage());
                        throw new AccountCreationException("Customer verification failed before retry");
                    }

                    // Delay before retry
                    delayMs(retryDelay);
                    log.info("Retrying {} account creation (attempt {} of {})", currency, attempt + 1, maxRetries);
                } else {
                    log.error("Max retries ({}) exceeded for {} account creation", maxRetries, currency);
                    throw e;
                }
            }
        }
        return null;
    }

    private void delayMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Delay interrupted: {}", e.getMessage());
        }
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

    // ─── Step 8: Validate At Least One Account Exists ────────────────────────
    public void validateAtLeastOneAccountExists(Map<String, String> customerInfo, String khrAccount,
                                                String usdAccount) {
        log.info(">>> Step 8: VALIDATE_ACCOUNT_CREATION");

        // Validate CIF exists
        String cif = customerInfo != null ? customerInfo.get("CIF") : null;
        if (cif == null || cif.isEmpty()) {
            throw new AccountCreationException("Customer CIF not found. Account creation failed.");
        }

        // Validate at least one account exists (USD or KHR)
        boolean khrExists = khrAccount != null || (customerInfo != null && validationService.hasAccount(customerInfo, AppConstants.CURRENCY_KHR));
        boolean usdExists = usdAccount != null || (customerInfo != null && validationService.hasAccount(customerInfo, AppConstants.CURRENCY_USD));

        if (!khrExists && !usdExists) {
            throw new AccountCreationException(AppConstants.FAIL_CREATE_ANY_ACCOUNT);
        }

        log.info("Step 8 SUCCESS: CIF validated and at least one account exists (KHR: {}, USD: {})", khrExists, usdExists);
    }

    // ─── Step 9: Activate Mobile Banking ─────────────────────────────────────
    public String activateMobileBanking(CustomerRequest request, String cif, String khrAccount, String usdAccount) {
        log.info(">>> Step 9: ACTIVATE_MOBILE_BANKING");
        try {
            String activationCode = mobileBankingService.activate(request, cif, khrAccount, usdAccount);
            log.info("Step 9 SUCCESS: Mobile banking activated");
            return activationCode;
        } catch (Exception e) {
            log.warn("Step 9 WARNING: Mobile banking activation failed (non-critical): {}", e.getMessage());
            return null;
        }
    }
}