package com.internal.feature.open_account.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AmlStatusEnum;
import com.internal.enumation.OpenAccStatusEnum;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.CustomerAmlDto;
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
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.feature.open_account.service.external.*;
import com.internal.feature.reference.dto.response.OccupationDto;
import com.internal.feature.reference.service.OccupationService;
import com.internal.feature.telegram_alerts.service.serviceImpl.OpenAccountTelegramAlertServiceImpl;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public CustomerResponse openAccount(CustomerRequest request) {
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

        try {
            // Step 2: Get customer info
            currentStep = "GET_CUSTOMER_INFO";
            Map<String, String> customerInfo = getCustomerInfo(request);

            // Step 3: Validate existing accounts
            currentStep = "VALIDATE_EXISTING_ACCOUNTS";
            validateExistingAccounts(customerInfo);

            // Step 4: Process AML (before account creation)
            currentStep = "PROCESS_AML";
            AmlStatusDto amlProcessResult = processAml(request);

            // Step 5: Create customer
            currentStep = "CREATE_CUSTOMER";
            cif = createCustomerIfNeeded(request, customerInfo);
            mnemonic = XmlParser.extractMnemonic(t24Service.createCustomer(request));

            // Step 6: Create KHR account
            currentStep = "CREATE_KHR_ACCOUNT";
            khrAccount = createAccountIfNeeded(request, customerInfo, cif, "KHR");

            // Step 7: Create USD account
            currentStep = "CREATE_USD_ACCOUNT";
            usdAccount = createAccountIfNeeded(request, customerInfo, cif, "USD");

            // Step 8: Validate at least one account created
            currentStep = "VALIDATE_ACCOUNT_CREATION";
            validateAtLeastOneAccountExists(customerInfo, khrAccount, usdAccount);

            // Step 9: Activate mobile banking
            currentStep = "ACTIVATE_MOBILE_BANKING";
            activateMobileBanking(request, cif, khrAccount, usdAccount);

            // Step 10: Update AML record with account info (non-blocking)
            currentStep = "UPDATE_AML_WITH_ACCOUNTS";
            updateAmlRecordWithAccounts(request.getLegalId(), cif, khrAccount, usdAccount, mnemonic);

            // Step 11: Save customer images (non-blocking)
            currentStep = "SAVE_CUSTOMER_IMAGES";
            CustomerImageUploadResponseDto imagePaths = safeSaveCustomerImages(request);

            // Step 12: Save success log (non-blocking)
            currentStep = "SAVE_FINAL_LOG";
            CustomerResponse accInfo = buildCustomerAccInfo(cif, khrAccount, usdAccount, mnemonic);
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

            // Step 14: Return success response
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

            String failureRemark = buildFailureRemark(currentStep, cif, khrAccount, usdAccount);
            reportLogService.createAccountOpeningLog(
                    request.getLegalId(),
                    OpenAccStatusEnum.FAILURE,
                    failureRemark,
                    e
            );

            throw e;
        }
    }

    private static CustomerResponse buildCustomerAccInfo(String cif, String khrAccount, String usdAccount, String mnemonic) {
        return CustomerResponse.builder()
                .cif(cif)
                .khrAccount(khrAccount)
                .usdAccount(usdAccount)
                .mnemonic(mnemonic)
                .build();
    }

    private Map<String, String> getCustomerInfo(CustomerRequest request) {
        log.info(">>> Step 2: GET_CUSTOMER_INFO");
        Map<String, String> customerInfo = validationService.getCustomerInfo(request.getLegalId());
        log.info("Customer info retrieved: {}", customerInfo != null ? "Found" : "Not found");
        return customerInfo;
    }

    private AmlStatusDto processAml(CustomerRequest request) {
        log.info(">>> Step 3: PROCESS_AML");
        try {
            AmlStatusDto dto = createAmlRecordAndNotify(request);
            log.info("Step 3 SUCCESS: AML processed successfully");
            return dto;
        } catch (Exception e) {
            log.error("Step 3 FAILED: AML processing failed: {}", e.getMessage());
            throw e; // Re-throw to stop account creation
        }
    }

    private void validateExistingAccounts(Map<String, String> customerInfo) {
        log.info(">>> Step 4: VALIDATE_EXISTING_ACCOUNTS");
        validationService.validateExistingAccounts(customerInfo);
        log.info("Existing accounts validation passed");
    }

    private String createCustomerIfNeeded(CustomerRequest request, Map<String, String> customerInfo) {
        String cif = customerInfo.get("CIF");
        if (cif != null && !cif.isEmpty()) {
            log.info("Existing CIF found: {}", cif);
            log.info("Step 5 SKIPPED: Using existing customer");
            return cif;
        }

        log.info(">>> Step 5: CREATE_CUSTOMER");
        log.info("No existing CIF found. Creating new customer in T24...");
        Document t24Response = t24Service.createCustomer(request);

        if (t24Response == null) {
            throw new AccountCreationException("T24 returned null response for customer creation");
        }
        if (XmlParser.hasError(t24Response)) {
            throw new AccountCreationException("T24 error: " + XmlParser.extractErrorMessage(t24Response));
        }

        cif = XmlParser.extractCif(t24Response);
        if (cif == null || cif.isEmpty()) {
            throw new AccountCreationException("No CIF returned from T24");
        }

        log.info("Step 5 SUCCESS: Customer created in T24 with CIF: {}", cif);
        return cif;
    }

    private String createAccountIfNeeded(CustomerRequest request, Map<String, String> customerInfo, String cif, String currency) {
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

    /**
     * Process AML with admin approval mechanism:
     * 1. Check if Legal ID exists in AML table
     * 2. If exists with APPROVE status → Skip middleware, continue account creation
     * 3. If exists with PENDING → Stop account creation, throw message to wait for admin approval
     * 4. If exists with REJECT → Stop account creation, throw rejection message
     * 5. If NOT exists → Call middleware, save as PENDING, stop account creation
     *    - Send notification ONLY if HIGH risk
     *    - Always require admin approval before account creation (PENDING status)
     */
    private AmlStatusDto createAmlRecordAndNotify(CustomerRequest request) {
        try {
            log.info(">>> Checking existing AML for Legal ID: {}", request.getLegalId());
            // STEP 1: Check if AML record already exists
            Optional<AmlStatus> existingAmlOpt = amlService.findByLegalId(request.getLegalId());

            existingAmlOpt.ifPresent(record -> log.info("Existing AML record found: Status={}", record.getStatus()));


            if (existingAmlOpt.isPresent()) {
                AmlStatus existing = existingAmlOpt.get();
                AmlStatusEnum status = existing.getStatus();

                log.info("AML record already exists for Legal ID: {} with status: {}",
                        request.getLegalId(), status);

                switch (status) {
                    case APPROVE:
                        // ✅ Admin approved - skip middleware, continue account creation
                        log.info("AML already APPROVED by admin. Skipping middleware check. Continuing account creation...");
                        return AmlStatusDto.builder()
                                .status(AmlStatusEnum.APPROVE)
                                .remarks("AML approved by admin - Account creation allowed")
                                .build();

                    case PENDING:
                        // ⏳ Waiting for admin approval - stop account creation
                        log.warn("AML status is PENDING for Legal ID: {}. Waiting for admin approval.",
                                request.getLegalId());
                        throw new AccountCreationException(
                                "AML check for Legal ID " + request.getLegalId() +
                                        " is PENDING admin approval. Please wait for manual review before creating account."
                        );

                    case REJECT:
                        // ❌ Admin rejected - stop account creation
                        log.warn("AML status is REJECTED for Legal ID: {}", request.getLegalId());
                        throw new AccountCreationException(
                                "AML check for Legal ID " + request.getLegalId() +
                                        " has been REJECTED by admin. Account cannot be created."
                        );

                    default:
                        throw new AccountCreationException(
                                "AML check for Legal ID " + request.getLegalId() +
                                        " has unknown status: " + status
                        );
                }
            }

            // STEP 2: No existing AML record - call middleware to check risk
            log.info("No existing AML record found. Calling middleware for Legal ID: {}",
                    request.getLegalId());

            CustomerAmlRequest amlRequestDto = buildAmlRequestDto(request);
            AmlExternalResponseDto amlResponse = callAmlMiddleware(amlRequestDto, request.getLegalId());

            // STEP 3: Save AML record as PENDING (requires admin approval)
            AmlStatusDto savedAmlStatus = saveAmlRecord(amlRequestDto, amlResponse, request, AmlStatusEnum.PENDING);

            log.info("AML record saved with PENDING status for Legal ID: {}", request.getLegalId());

            // STEP 4: Send notification if HIGH risk
            boolean isHighRisk = "High".equalsIgnoreCase(amlResponse.getRiskLevel());
            if (isHighRisk) {
                log.warn("HIGH RISK detected for Legal ID: {}. Sending notification to admin...",
                        request.getLegalId());
                sendAmlNotification(amlRequestDto, amlResponse, request);
            } else {
                log.info("LOW risk detected for Legal ID: {}. No notification sent.",
                        request.getLegalId());
            }

            // STEP 5: Always stop account creation - require admin approval first
            throw new AccountCreationException(
                    "AML screening completed for Legal ID " + request.getLegalId() +
                            ". Risk Level: " + amlResponse.getRiskLevel() +
                            ". Status set to PENDING. Please wait for admin approval before creating account."
            );

        } catch (AccountCreationException e) {
            throw e; // Propagate specific messages to controller
        } catch (Exception e) {
            log.error("Unexpected error in AML processing for Legal ID {}: {}",
                    request.getLegalId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process AML check", e);
        }
    }

    private CustomerAmlRequest buildAmlRequestDto(CustomerRequest request) {
        return CustomerAmlRequest.builder()
                .customerId(request.getLegalId())
                .custCreateDate(request.getLegalIssueDate())
                .customerType("ACTIVE")
                .custName(request.getFamilyName() + " " + request.getGivenName())
                .givenName(request.getGivenName())
                .familyName(request.getFamilyName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .nationality("KH")
                .legalAddress(request.getLegalAddress() != null ? request.getLegalAddress() : "NA")
                .custDistrict(request.getCustomerPobDistrict())
                .custProvince(request.getCustomerPobProvince())
                .country("Cambodia")
                .sms1(null)
                .phoneNumber(request.getPhoneNumber())
                .offPhone(null)
                .occupation(request.getOccupation())
                .legalId(request.getLegalId() + "-NATIONAL.ID")
                .maritalStatus(request.getMaritalStatus())
                .businessSector(null)
                .target("220")
                .income(0)
                .dobYear(null)
                .dobMonth(null)
                .dobDay(null)
                .legalDocName("NATIONAL.ID")
                .legalExpDate(request.getLegalExpireDate())
                .customerRating("1")
                .build();
    }

    private AmlExternalResponseDto callAmlMiddleware(CustomerAmlRequest amlRequest, String legalId) throws JsonProcessingException {
        AmlExternalResponseDto response = amlMiddlewareService.CheckAml(amlRequest);
        log.info("AML Middleware response received | RiskLevel: {} | TrxnID: {}",
                response.getRiskLevel(), response.getTrxnID());
        return response;
    }

    private AmlStatusDto saveAmlRecord(CustomerAmlRequest amlRequest,
                                       AmlExternalResponseDto amlResponse,
                                       CustomerRequest request,
                                       AmlStatusEnum status) throws JsonProcessingException {

        OccupationDto occupationDto = safeOccupationLookup(request.getOccupation());

        String occupationCode = request.getOccupation();
        String occupationStatus = occupationDto != null
                ? occupationDto.getNameEn() + " / " + occupationDto.getNameKh()
                : "UNKNOWN";

        CreateAmlRequestDto createRequest = CreateAmlRequestDto.builder()
                // Set status based on risk level (PENDING for manual admin approval)
                .status(status)

                // Personal info
                .legalId(request.getLegalId())
                .familyName(request.getFamilyName())
                .givenName(request.getGivenName())
                .firstNameKh(request.getFirstNameKh())
                .lastNameKh(request.getLastNameKh())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .nationality("KH")
                .legalAddress(request.getLegalAddress())
                .issuedDate(request.getLegalIssueDate())
                .expiredDate(request.getLegalExpireDate())

                // Contact & other personal
                .phoneNumber(request.getPhoneNumber())
                .maritalStatus(request.getMaritalStatus())
                .occupationCode(occupationCode)
                .occupationStatus(occupationStatus)

                // Customer current address
                .customerCurrentProvince(request.getCustomerCurrentProvince())
                .customerCurrentDistrict(request.getCustomerCurrentDistrict())
                .customerCurrentCommune(request.getCustomerCurrentCommune())
                .customerCurrentVillage(request.getCustomerCurrentVillage())

                // Place of birth
                .customerPobProvince(request.getCustomerPobProvince())
                .customerPobDistrict(request.getCustomerPobDistrict())
                .customerPobCommune(request.getCustomerPobCommune())
                .customerPobVillage(request.getCustomerPobVillage())

                // AML external results
                .screeningResult(objectMapper.writeValueAsString(amlResponse))
                .riskLevel(amlResponse.getRiskLevel())
                .actionTaken(amlResponse.getActionTaken())
                .rulesTriggered(objectMapper.writeValueAsString(amlResponse.getRulesTriggered()))
                .serviceName(amlResponse.getServiceName())
                .totalRulesScore(amlResponse.getTotalRulesScore())
                .trxnID(amlResponse.getTrxnID())

                .build();

        return amlService.createAmlStatus(createRequest);
    }

    /**
     * Send Email/Telegram notification
     * Called ONLY when Risk Level is HIGH
     */
    private void sendAmlNotification(CustomerAmlRequest amlRequest,
                                     AmlExternalResponseDto amlResponse,
                                     CustomerRequest request) {

        // 🔹 Log entry and key status info
        log.info(">>> ENTER sendAmlNotification() for Legal ID: {}", request.getLegalId());
        log.info("AML Risk Level: {}", amlResponse.getRiskLevel());
        log.info("AML Status (from request context / expected): PENDING");

        try {
            // Build AML DTO payload
            AmlStatusDto amlDto = AmlStatusDto.builder()
                    .status(AmlStatusEnum.PENDING)
                    .customerInfo(CustomerAmlDto.builder()
                            .legalId(request.getLegalId())
                            .givenName(request.getGivenName())
                            .familyName(request.getFamilyName())
                            .firstNameKh(request.getFirstNameKh())
                            .lastNameKh(request.getLastNameKh())
                            .dateOfBirth(request.getDateOfBirth())
                            .gender(request.getGender())
                            .placeOfBirth(request.getPlaceOfBirth())
                            .phoneNumber(request.getPhoneNumber())
                            .nationality("KH")
                            .legalAddress(request.getLegalAddress())
                            .build())
                    .riskLevel(amlResponse.getRiskLevel())
                    .actionTaken(amlResponse.getActionTaken())
                    .serviceName(amlResponse.getServiceName())
                    .totalRulesScore(amlResponse.getTotalRulesScore())
                    .rulesTriggered(amlResponse.getRulesTriggered())
                    .trxnID(amlResponse.getTrxnID())
                    .build();

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

    private String asJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
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