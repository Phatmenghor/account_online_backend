package com.internal.feature.open_account.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.config.TestProperties;
import com.internal.enumation.AmlStatusEnum;
import com.internal.enumation.OpenAccStatusEnum;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.aml.service.AmlService;
import com.internal.feature.logs_report.dto.request.CustomerFileUploadRequestDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.service.AccountOnlineOpenFinalService;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.logs_report.service.CustomerImageService;
import com.internal.feature.mail.service.MailService;
import com.internal.feature.open_account.dto.request.CustomerAmlRequest;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.AmlExternalResponseDto;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.mapper.OpenAccountAmlStatusMapper;
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.feature.open_account.service.external.*;
import com.internal.feature.reference.dto.response.OccupationDto;
import com.internal.feature.reference.service.OccupationService;
import com.internal.feature.telegram_alerts.service.serviceImpl.OpenAccountTelegramAlertServiceImpl;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountServiceImpl implements OpenAccountService {

    private final ValidationService validationService;
    private final T24Service t24Service;
    private final MobileBankingService mobileBankingService;
    private final AccountOnlineReportLogService reportLogService;
    private final AmlService amlService;
    private final MailService mailService;
    private final CustomerImageService customerImageService;
    private final AccountOnlineOpenFinalService accountOnlineOpenSuccessService;
    private final ObjectMapper objectMapper;
    private final AmlMiddlewareService amlMiddlewareService;
    private final OccupationService occupationService;
    private final OpenAccountTelegramAlertServiceImpl alertTelegramService;
    private final OpenAccountAmlStatusMapper openAccountAmlStatusMapper;
    private final TestProperties isTestMode;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public CustomerResponse openAccount(CustomerRequest request) throws Exception {
        log.info("========== ACCOUNT OPENING STARTED ==========");
        log.info("Legal ID: {}", request.getLegalId());
        log.info("Name: {} {} ({} {})", request.getGivenName(), request.getFamilyName(),
                request.getFirstNameKh(), request.getLastNameKh());
        log.info("Phone: {}", request.getPhoneNumber());
        log.info("==============================================");

        String currentStep = "START";
        String cif = null;
        String mnemonic = null;
        String khrAccount = null;
        String usdAccount = null;
        AmlStatusDto amlProcessResult = null;

        try {
            // Step 1: Test connection
            currentStep = AppConstants.TEST_CONNECTION;
            testConnection();

            // Step 2: Get customer info
            currentStep = AppConstants.GET_CUSTOMER_INFO;
            Map<String, String> customerInfo = getCustomerInfo(request);

            // Step 4: Process AML (before account creation)
            currentStep = AppConstants.PROCESS_AML;
            amlProcessResult = processAml(request);

            // Abort if high-risk
            sentMessageOnHighRisk(request, amlProcessResult);

            // Step 5: Create customer
            currentStep = AppConstants.CREATE_CUSTOMER;
            cif = createCustomerIfNeeded(request, customerInfo);
            mnemonic = XmlParser.extractMnemonic(t24Service.createCustomer(request));

            // Step 6: Create KHR account
            currentStep = AppConstants.CREATE_KHR_ACCOUNT;
            khrAccount = createAccountIfNeeded(request, customerInfo, cif, "KHR");

            // Step 7: Create USD account
            currentStep = AppConstants.CREATE_USD_ACCOUNT;
            usdAccount = createAccountIfNeeded(request, customerInfo, cif, "USD");

            // Step 8: Validate accounts
            currentStep = AppConstants.VALIDATE_ACCOUNT_CREATION;
            validateAtLeastOneAccountExists(customerInfo, khrAccount, usdAccount);

            // Step 9: Activate mobile banking
            currentStep = AppConstants.ACTIVATE_MOBILE_BANKING;
            activateMobileBanking(request, cif, khrAccount, usdAccount);

            // Step 10: Update AML records (non-blocking)
            currentStep = AppConstants.UPDATE_AML_WITH_ACCOUNTS;
            updateAmlRecordWithAccounts(request.getLegalId(), cif, khrAccount, usdAccount, mnemonic);

            // Step 11: Save customer images (non-blocking)
            currentStep = AppConstants.SAVE_CUSTOMER_IMAGES;
            CustomerImageUploadResponseDto imagePaths = safeSaveCustomerImages(request);

            // Step 12: Save success log (non-blocking)
            currentStep = AppConstants.SAVE_FINAL_LOG;
            CustomerResponse accInfo = openAccountAmlStatusMapper.buildCustomerAccInfo(cif, khrAccount, usdAccount, mnemonic);
            safeSaveSuccessLog(request, accInfo, amlProcessResult, imagePaths);

            // Step 13: Report log
            reportLogService.saveLogReport(
                    request.getLegalId(),
                    OpenAccStatusEnum.SUCCESS,
                    "Open account online Successfully"
            );

            log.info("========== ACCOUNT OPENING COMPLETED ==========");
            log.info("CIF: {}", cif);
            log.info("KHR Account: {}", khrAccount);
            log.info("USD Account: {}", usdAccount);
            log.info("Mnemonic: {}", mnemonic);
            log.info("===============================================");

            return accInfo;

        } catch (Exception e) {
            log.error("========== ACCOUNT OPENING FAILED ==========");
            log.error("Failed at step: {}", currentStep);
            log.error("Legal ID: {}", request.getLegalId());
            log.error("Error: {}", e.getMessage());
            log.error("CIF: {}", cif);
            log.error("KHR Account: {}", khrAccount);
            log.error("USD Account: {}", usdAccount);
            log.error("============================================");

            // ⚠️ AML process result used here for logging high-risk or failure
            String failureRemark = buildFailureRemark(currentStep, cif, khrAccount, usdAccount, amlProcessResult);
            saveFailureLogs(request, e, currentStep, failureRemark);

            throw e;
        }
    }

    private static void sentMessageOnHighRisk(CustomerRequest request, AmlStatusDto amlProcessResult) {
        if (amlProcessResult.getStatus() == AmlStatusEnum.PENDING) {
            throw new AccountCreationException(
                    String.format(AppConstants.AML_NEED_REVIEW_MSG, request.getLegalId())
            );
        } else if (amlProcessResult.getStatus() == AmlStatusEnum.REJECT) {
            throw new AccountCreationException(
                    String.format(AppConstants.AML_REJECTED_MSG, request.getLegalId())
            );
        }
    }

    private void saveFailureLogs(CustomerRequest request, Exception e, String currentStep, String failureRemark) {
        if (currentStep.equals(AppConstants.PROCESS_AML)) {
            reportLogService.createAccountOpeningLog(
                    request.getLegalId(),
                    OpenAccStatusEnum.AML,
                    failureRemark,
                    e
            );
        } else {
            reportLogService.createAccountOpeningLog(
                    request.getLegalId(),
                    OpenAccStatusEnum.FAILURE,
                    failureRemark,
                    e
            );
        }
    }

    /**
     * Step 1: Test DB connection by executing a simple query.
     * Throws RuntimeException if connection fails.
     */
    private void testConnection() {
        log.info(">>> Step 1: TEST_CONNECTION");
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("Step 1 SUCCESS: Database connection is healthy");
        } catch (Exception e) {
            log.error("Step 1 FAILED: Database connection test failed", e);
            throw new RuntimeException(
                    "Unable to connect to the server. Please try again later. " +
                            "If the issue continues, contact our support team at 070 200 002 or 1800 200 888."
            );
        }
    }

    private Map<String, String> getCustomerInfo(CustomerRequest request) {
        log.info(">>> Step 2: GET_CUSTOMER_INFO");
        Map<String, String> customerInfo = validationService.getCustomerInfo(request.getLegalId());
        log.info("Customer info retrieved: {}", customerInfo != null ? "Found" : "Not found");
        return customerInfo;
    }

    private AmlStatusDto processAml(CustomerRequest request) throws Exception {

            // 1️⃣ Check for existing AML
            Optional<AmlStatus> existingAmlOpt = amlService.findByLegalId(request.getLegalId());

            if (existingAmlOpt.isPresent()) {
                return handleExistingAml(existingAmlOpt.get(), request.getLegalId());
            }

            // 2️⃣ Build request and call AML middleware
            CustomerAmlRequest amlRequestDto = openAccountAmlStatusMapper.buildAmlRequestDto(request);
            AmlExternalResponseDto amlResponse = callAmlMiddleware(amlRequestDto, request.getLegalId());

            // 3️⃣ Build occupation status string
            String occupationStatus = buildOccupationStatus(request.getOccupation());

            // 4️⃣ Determine AML status based on risk
            boolean isHighRisk = AppConstants.HIGH_RISK.equalsIgnoreCase(amlResponse.getRiskLevel());
            AmlStatusEnum amlStatusEnum = isHighRisk ? AmlStatusEnum.PENDING : AmlStatusEnum.APPROVE;

            // 5️⃣ Map to CreateAmlRequestDto
            CreateAmlRequestDto createRequest = openAccountAmlStatusMapper.toCreateRequest(
                    amlRequestDto,
                    amlResponse,
                    request,
                    occupationStatus,
                    amlStatusEnum,
                    objectMapper
            );

            // 6️⃣ Handle high-risk customers
            if (isHighRisk) {
                amlService.createAmlStatus(createRequest);
                sendAmlNotification(amlRequestDto, amlResponse, request); // ⚠️ Keep comment
            }

            // 7️⃣ Low-risk → return mapped DTO
            return openAccountAmlStatusMapper.fromRequestAndResponse(request, amlResponse, amlStatusEnum);
    }

    private AmlStatusDto handleExistingAml(AmlStatus existing, String legalId) {
        switch (existing.getStatus()) {
            case APPROVE: return openAccountAmlStatusMapper.toDto(existing);
            case PENDING: throw new AccountCreationException(
                    String.format(AppConstants.AML_NEED_REVIEW_MSG, legalId)
            );
            case REJECT: throw new AccountCreationException(
                    String.format(AppConstants.AML_REJECTED_MSG, legalId)
            );
            default: throw new AccountCreationException(
                    String.format(AppConstants.AML_UNKNOWN_MSG, legalId)
            );
        }
    }

    private String buildOccupationStatus(String occupationCode) {
        OccupationDto occupation = safeOccupationLookup(occupationCode);
        return occupation != null ? occupation.getNameEn() + " / " + occupation.getNameKh() : "";
    }

    private void validateExistingAccounts(Map<String, String> customerInfo) {
        log.info(">>> Step 4: VALIDATE_EXISTING_ACCOUNTS");
        validationService.validateExistingAccounts(customerInfo);
        log.info("Existing accounts validation passed");
    }

    private String createCustomerIfNeeded(CustomerRequest request, Map<String, String> customerInfo) {

        if (isTestMode.isSkipCheckCif()) {
            log.info("TEST MODE ENABLED — Always creating new customer, ignoring existing CIF.");
            Document resp = t24Service.createCustomer(request);
            return XmlParser.extractCif(resp);
        }

        // Normal production logic
        String existingCif = customerInfo.get("CIF");
        if (existingCif != null && !existingCif.isEmpty()) {
            log.info("Existing CIF found → Using existing customer");
            return existingCif;
        }

        Document resp = t24Service.createCustomer(request);
        return XmlParser.extractCif(resp);
    }

    private String createAccountIfNeeded(CustomerRequest request, Map<String, String> customerInfo, String cif, String currency) {

        if (isTestMode.isSkipCheckAccount()) {
            log.info("TEST MODE ENABLED — Skipping existing account check → creating new {} account", currency);
            return createAccount(request, cif, currency);
        }

        // Normal production check
        if (validationService.hasAccount(customerInfo, currency)) {
            log.info(">>> Step {}: CREATE_{}_ACCOUNT - SKIPPED (already exists)",
                    currency.equals("KHR") ? 6 : 7, currency);
            return null;
        }

        log.info(">>> Step {}: CREATE_{}_ACCOUNT",
                currency.equals("KHR") ? 6 : 7, currency);
        String account = createAccount(request, cif, currency);
        if (account != null) {
            log.info("Step {} SUCCESS: {} account created: {}",
                    currency.equals("KHR") ? 6 : 7, currency, account);
        } else {
            log.warn("Step {} FAILED: {} account creation returned null",
                    currency.equals("KHR") ? 6 : 7, currency);
        }
        return account;
    }

    private void validateAtLeastOneAccountExists(Map<String, String> customerInfo, String khrAccount, String usdAccount) {
        log.info(">>> Step 8: VALIDATE_ACCOUNT_CREATION");
        if (khrAccount == null && usdAccount == null &&
                !validationService.hasAccount(customerInfo, "KHR") &&
                !validationService.hasAccount(customerInfo, "USD")) {
            throw new AccountCreationException(AppConstants.FAIL_CREATE_ANY_ACCOUNT);
        }
        log.info("Step 8 SUCCESS: At least one account exists");
    }

    private void activateMobileBanking(CustomerRequest request, String cif, String khrAccount, String usdAccount) {
        log.info(">>> Step 9: ACTIVATE_MOBILE_BANKING");
        try {
            mobileBankingService.activate(request, cif, khrAccount, usdAccount);
            log.info("Step 9 SUCCESS: Mobile banking activated");
        } catch (Exception e) {
            log.warn("Step 9 WARNING: Mobile banking activation failed (non-critical): {}", e.getMessage());
        }
    }

    private void updateAmlRecordWithAccounts(String legalId, String cif, String khrAccount, String usdAccount, String mnemonic) {
        log.info(">>> Step 10: UPDATE_AML_WITH_ACCOUNTS");
        try {
            Optional<AmlStatus> amlRecord = amlService.findByLegalId(legalId);
            log.info("Found existing AML? {} | Status: {}",
                    amlRecord.isPresent(),
                    amlRecord.map(AmlStatus::getStatus).orElse(null));
            if (amlRecord.isPresent()) {
                // Update the AML record with account information if needed
                log.info("Step 10 SUCCESS: AML record found and can be updated with account info");
            } else {
                log.warn("Step 10 WARNING: No AML record found to update");
            }
        } catch (Exception e) {
            log.warn("Step 10 WARNING: Failed to update AML record (non-critical): {}", e.getMessage());
        }
    }

    private CustomerImageUploadResponseDto safeSaveCustomerImages(CustomerRequest request) {
        log.info(">>> Step 11: SAVE_CUSTOMER_IMAGES");
        try {
            CustomerFileUploadRequestDto fileRequest = CustomerFileUploadRequestDto.builder()
                    .legal_id(request.getLegalId())
                    .NidImage(request.getNidImage())
                    .SelfieImage(request.getSelfieImage())
                    .build();

            CustomerImageUploadResponseDto imagePaths = customerImageService.saveCustomerImages(fileRequest);
            log.info("Step 11 SUCCESS: Images saved");
            return imagePaths;
        } catch (Exception e) {
            log.warn("Step 11 WARNING: Failed to save images (non-critical): {}", e.getMessage());
            return null;
        }
    }

    private void safeSaveSuccessLog(
            CustomerRequest request,
            CustomerResponse accountInfo,
            AmlStatusDto amlStatusResponseDto,
            CustomerImageUploadResponseDto imagePaths
    ) {
        log.info(">>> Step 12: SAVE_SUCCESS_LOG");
        try {
            accountOnlineOpenSuccessService.saveFinalLog(request, accountInfo, amlStatusResponseDto, imagePaths);
            log.info("Step 12 SUCCESS: Success log saved");
        } catch (Exception e) {
            log.warn("Step 12 WARNING: Failed to save success log (non-critical): {}", e.getMessage());
        }
    }

    private AmlExternalResponseDto callAmlMiddleware(CustomerAmlRequest amlRequest, String legalId) throws JsonProcessingException {
        AmlExternalResponseDto response = amlMiddlewareService.CheckAml(amlRequest);
        log.info("AML Middleware response received | RiskLevel: {} | TrxnID: {}",
                response.getRiskLevel(), response.getTrxnID());
        return response;
    }

    /**
     * Send Email/Telegram notification
     * Called ONLY when Risk Level is HIGH
     */
    private void sendAmlNotification(CustomerAmlRequest amlRequest,
                                     AmlExternalResponseDto amlResponse,
                                     CustomerRequest request) {

        log.info(">>> ENTER sendAmlNotification() for Legal ID: {}", request.getLegalId());
        log.info("AML Risk Level: {}", amlResponse.getRiskLevel());
        log.info("AML Status (from request context / expected): PENDING");

        try {
            // Use mapper to build the AML DTO payload
            AmlStatusDto amlDto = openAccountAmlStatusMapper.fromRequestAndResponse(
                    request,
                    amlResponse,
                    AmlStatusEnum.PENDING
            );

            // Telegram Notification
            log.info(">>> Attempting Telegram notification for Legal ID: {}", request.getLegalId());
            try {
                alertTelegramService.sendTelegramAmlProcess(amlDto);
                log.info("Telegram AML notification sent successfully.");
            } catch (Exception e) {
                log.error("Telegram notification failed: {}", e.getMessage());
            }

            // Email Notification
            log.info(">>> Attempting Email notification for Legal ID: {}", request.getLegalId());
            try {
                mailService.sendAmlStatusNotification(amlDto);
                log.info("Email AML notification sent successfully.");
            } catch (Exception e) {
                log.error("Email notification failed: {}", e.getMessage());
            }

            log.info("<<< sendAmlNotification() completed for Legal ID: {}", request.getLegalId());

        } catch (Exception e) {
            log.error("Notification logic failed for Legal ID {}: {}", request.getLegalId(), e.getMessage());
        }
    }

    private String buildFailureRemark(String failedStep, String cif, String khrAccount, String usdAccount, AmlStatusDto amlProcessResult) {
        StringBuilder remark = new StringBuilder("Account opening failed at: ").append(failedStep);

        // Append AML status if it's not APPROVE and amlProcessResult is not null
        if (amlProcessResult != null && amlProcessResult.getStatus() != null
                && !amlProcessResult.getStatus().equals(AmlStatusEnum.APPROVE)) {
            remark.append(" | AML Status: ").append(amlProcessResult.getStatus());
        }

        // Append account info if present
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

    private OccupationDto safeOccupationLookup(String code) {
        try { return code != null ? occupationService.getOccupationByCode(code) : null; }
        catch (Exception e) { log.warn("⚠️ Occupation lookup failed for code {}", code); return null; }
    }
}