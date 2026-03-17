package com.internal.feature.open_account.facade;

import com.internal.config.TestProperties;
import com.internal.exceptions.error.custom.NidValidationException;
import com.internal.exceptions.error.custom.ValidateServiceException;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import com.internal.feature.logs_report.repository.AccountOnlineFinalRepository;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankingService {

    private final ValidationService validationService;
    private final T24Service t24Service;
    private final MobileBankingService mobileBankingService;
    private final JdbcTemplate jdbcTemplate;
    private final TestProperties isTestMode;
    private final AccountOnlineFinalRepository accountOnlineFinalRepository;

    @Value("${simulator.banking.camdx-error:false}")
    private boolean simulateCamdxError;

    @Value("${simulator.banking.internal-error:false}")
    private boolean simulateInternalError;

    // ─── Step 1: Test Connection ──────────────────────────────────────────────
    public void testConnection() {
        if (simulateInternalError) {
            throw new ValidateServiceException("Simulated Internal T24 Connection Error (SIMULATION)");
        }

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        } catch (Exception e) {
            log.error("Database connection test failed", e);
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

    // ─── Step 2: Get Existing Account Details ─────────────────────────────────
    /**
     * Retrieves complete account details from acc_online_open_final table.
     * Used for returning full response in recovery path.
     */
    public Optional<AccountOnlineFinal> getExistingAccountDetails(String legalId) {
        return accountOnlineFinalRepository.findByLegalId(legalId);
    }

    // ─── Step 2.5: Check Existing Complete Account (Recovery) ───────────────────
    /**
     * Checks if customer already has complete accounts (USD + KHR + CIF) in the final table.
     * If accounts exist but MB activation code is null, activates and sends SMS.
     * If one or more fields are missing, returns empty (continue normal flow).
     */
    public Optional<String> checkExistingCompleteAccountAndActivate(CustomerRequest request) {
        log.info(">>> Step 2.5: CHECK_EXISTING_COMPLETE_ACCOUNT_RECOVERY");

        Optional<AccountOnlineFinal> existingAccount = accountOnlineFinalRepository.findByLegalId(request.getLegalId());

        if (existingAccount.isEmpty()) {
            log.info("No existing account found → Continue with normal account creation flow");
            return Optional.empty();
        }

        AccountOnlineFinal account = existingAccount.get();

        // Check if ALL required fields exist
        boolean hasAllFields = account.getCif() != null && !account.getCif().isEmpty()
                && account.getKhrAccount() != null && !account.getKhrAccount().isEmpty()
                && account.getUsdAccount() != null && !account.getUsdAccount().isEmpty()
                && account.getLegalId() != null && !account.getLegalId().isEmpty();

        if (!hasAllFields) {
            log.info("Some account fields are missing → Continue with normal account creation flow");
            return Optional.empty();
        }

        log.info("✓ Complete accounts found: CIF={}, KHR={}, USD={}", account.getCif(), account.getKhrAccount(), account.getUsdAccount());

        // Check if MB activation code is missing
        if (account.getMbActivationCode() == null || account.getMbActivationCode().isEmpty()) {
            log.warn("MB activation code is null → Attempting activation recovery...");

            try {
                String activationCode = mobileBankingService.activate(request, account.getCif(),
                        account.getKhrAccount(), account.getUsdAccount());

                if (activationCode != null && !activationCode.isEmpty()) {
                    log.info("✓ MB activation successful → Code: {}", activationCode);

                    // Update the final table with the new activation code
                    try {
                        account.setMbActivationCode(activationCode);
                        accountOnlineFinalRepository.save(account);
                        log.info("✓ Updated acc_online_open_final table with activation code");
                    } catch (Exception saveError) {
                        log.error("Failed to save activation code to database: {}", saveError.getMessage());
                        // Don't fail the whole recovery if DB save fails
                    }

                    return Optional.of(activationCode);
                } else {
                    log.warn("MB activation returned null code → Continue to Step 3");
                    return Optional.empty();
                }
            } catch (Exception e) {
                log.error("MB activation recovery failed: {} → Continue to Step 3", e.getMessage());
                return Optional.empty();
            }
        }

        log.info("✓ Complete account already has activation code → Account opening complete!");
        return Optional.of(account.getMbActivationCode());
    }

    // ─── Step 3: Validate Existing Accounts ───────────────────────────────────
    public void validateExistingAccounts(Map<String, String> customerInfo) {
        validationService.validateExistingAccounts(customerInfo);
    }

    // ─── Step 5: Create Customer ──────────────────────────────────────────────
    /**
     * Creates a new customer in T24 if no existing CIF is found.
     * Extracts both CIF and MNEMONIC from a SINGLE t24Service.createCustomer() call.
     * For existing customers, both CIF and MNEMONIC are taken directly from customerInfo.
     * Includes retry logic with customer verification before each retry.
     */
    public CustomerCreationResult createCustomerIfNeeded(CustomerRequest request, Map<String, String> customerInfo) {
        String existingCif = customerInfo.get("CIF");

        if (existingCif != null && !existingCif.isEmpty()) {
            log.info("Existing CIF found → Using existing customer");
            String existingMnemonic = customerInfo.get("MNEMONIC");
            log.info("Existing MNEMONIC retrieved from customerInfo: {}", existingMnemonic);
            return new CustomerCreationResult(existingCif, existingMnemonic);
        }

        // Create new customer with retry logic
        return createCustomerWithRetry(request);
    }

    private CustomerCreationResult createCustomerWithRetry(CustomerRequest request) {
        int maxRetries = 3;
        int retryDelay = 3000; // 3 seconds

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Calling t24Service.createCustomer() - Attempt {} of {}", attempt, maxRetries);
                Document resp = t24Service.createCustomer(request);
                String cif = XmlParser.extractCif(resp);
                String mnemonic = XmlParser.extractMnemonic(resp);
                log.info("New customer created → CIF: {}, MNEMONIC: {}", cif, mnemonic);
                return new CustomerCreationResult(cif, mnemonic);
            } catch (Exception e) {
                log.warn("Attempt {} failed to create customer: {}", attempt, e.getMessage());

                if (attempt < maxRetries) {
                    // Delay 3 seconds before verification
                    delayMs(retryDelay);
                    log.info("Delayed 3 seconds. Verifying customer state before retry...");

                    // Verify customer info - check if customer was created despite error
                    try {
                        Map<String, String> freshCustomerInfo = getCustomerInfo(request.getLegalId());

                        // Check if customer already exists
                        String createdCif = freshCustomerInfo != null ? freshCustomerInfo.get("CIF") : null;
                        if (createdCif != null && !createdCif.isEmpty()) {
                            log.info("✓ Customer found in system → CIF: {}", createdCif);
                            String mnemonic = freshCustomerInfo.get("MNEMONIC");
                            return new CustomerCreationResult(createdCif, mnemonic);
                        }

                        // Customer not found - will retry API call
                        log.info("✗ Customer NOT found in system. Retrying t24Service.createCustomer() - Attempt {} of {}", attempt + 1, maxRetries);
                    } catch (Exception verifyError) {
                        log.error("Failed to verify customer info before retry: {}", verifyError.getMessage());
                        throw new AccountCreationException("Customer verification failed before retry");
                    }
                } else {
                    log.error("Max retries ({}) exceeded for customer creation", maxRetries);
                    throw e;
                }
            }
        }
        throw new AccountCreationException("Customer creation failed after all retries");
    }

    // ─── Steps 6 & 7: Create Accounts ────────────────────────────────────────
    public String createAccountIfNeeded(CustomerRequest request, Map<String, String> customerInfo, String cif,
                                        String currency) {
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
                log.info("Calling t24Service.createAccount({}) - Attempt {} of {}", currency, attempt, maxRetries);
                return createAccount(request, cif, currency);
            } catch (Exception e) {
                log.warn("Attempt {} failed to create {} account: {}", attempt, currency, e.getMessage());

                if (attempt < maxRetries) {
                    // Delay 3 seconds before verification
                    delayMs(retryDelay);
                    log.info("Delayed 3 seconds. Verifying customer state before retry...");

                    // Verify customer info and check if account was created despite error
                    try {
                        Map<String, String> freshCustomerInfo = getCustomerInfo(request.getLegalId());

                        // Check if account was actually created despite error
                        if (validationService.hasAccount(freshCustomerInfo, currency)) {
                            String existingAccount = freshCustomerInfo.get(currency);
                            log.info("✓ {} account found in system → returning: {}", currency, existingAccount);
                            return existingAccount;
                        }

                        // Account not found - will retry API call
                        log.info("✗ {} account NOT found in system. Retrying t24Service.createAccount({}) - Attempt {} of {}", currency, currency, attempt + 1, maxRetries);
                    } catch (Exception verifyError) {
                        log.error("Failed to verify customer info before retry: {}", verifyError.getMessage());
                        throw new AccountCreationException("Customer verification failed before retry");
                    }
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

    // ─── Step 9: Validate All Required Accounts Created ────────────────────────
    public void validateAllRequiredAccountsCreated(String cif, String khrAccount, String usdAccount) {
        // Validate CIF exists
        if (cif == null || cif.isEmpty()) {
            throw new AccountCreationException("Customer CIF not found. Account creation failed.");
        }

        // Validate both USD and KHR accounts exist
        if (khrAccount == null || khrAccount.isEmpty()) {
            throw new AccountCreationException("KHR account not found. Account creation failed.");
        }
        if (usdAccount == null || usdAccount.isEmpty()) {
            throw new AccountCreationException("USD account not found. Account creation failed.");
        }
    }

    // ─── Step 10: Activate Mobile Banking ────────────────────────────────────
    public String activateMobileBanking(CustomerRequest request, String cif, String khrAccount, String usdAccount) {
        try {
            return mobileBankingService.activate(request, cif, khrAccount, usdAccount);
        } catch (Exception e) {
            log.warn("Mobile banking activation failed (non-critical): {}", e.getMessage());
            return null;
        }
    }
}