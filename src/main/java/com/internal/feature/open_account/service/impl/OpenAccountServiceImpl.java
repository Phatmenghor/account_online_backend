package com.internal.feature.open_account.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AmlStatusEnum;
import com.internal.enumation.OpenAccStatusEnum;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.mapper.AmlStatusMapper;
import com.internal.feature.aml.service.AmlService;
import com.internal.feature.logs_report.dto.request.CustomerFileUploadRequestDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.service.AccountOnlineOpenSuccessService;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.logs_report.service.CustomerImageService;
import com.internal.feature.mail.service.MailService;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.feature.open_account.service.external.MobileBankingService;
import com.internal.feature.open_account.service.external.T24Service;
import com.internal.feature.open_account.service.external.ValidationService;
import com.internal.feature.open_account.service.external.XmlParser;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    private final AmlService amlService;
    private final MailService mailService;
    private final CustomerImageService customerImageService;
    private final AccountOnlineOpenSuccessService accountOnlineOpenSuccessService;
    private final AmlStatusMapper amlStatusMapper;
    private final ObjectMapper objectMapper;

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

            // Step 9: Activate mobile banking (non-blocking)
            currentStep = "ACTIVATE_MOBILE_BANKING";
            try {
                mobileBankingService.activate(request, cif, khrAccount, usdAccount);
                log.info("Mobile banking activation completed for CIF: {}", cif);
            } catch (Exception e) {
                log.error("Mobile banking activation failed (non-critical) for CIF {}: {}", cif, e.getMessage());
            }

            // Step 10: Create AML record in PENDING status and send notification
            currentStep = "CREATE_AML_AND_NOTIFY";
            try {
                createAmlRecordAndNotify(request, cif, khrAccount, usdAccount, mnemonic);
                log.info("AML record created and notification sent for Legal ID: {}", request.getLegalId());
            } catch (Exception e) {
                log.error("Failed to create AML record or send notification for Legal ID {}: {}",
                        request.getLegalId(), e.getMessage());
            }

            // Step 11: Save customer images
            currentStep = "SAVE_CUSTOMER_IMAGES";
            CustomerFileUploadRequestDto fileRequest = CustomerFileUploadRequestDto.builder()
                    .legal_id(request.getLegalId())
                    .NidImage(request.getNidImage())
                    .SelfieImage(request.getSelfieImage())
                    .build();

            CustomerImageUploadResponseDto imagePaths = customerImageService.saveCustomerImages(fileRequest);
            log.info("Customer images saved: NID={}, Selfie={}",
                    imagePaths.getNidImagePath(), imagePaths.getSelfieImagePath());

            // Step 12: Save success log
            currentStep = "SAVE_SUCCESS_LOG";
            accountOnlineOpenSuccessService.saveSuccessLog(request, imagePaths);
            log.info("Success log saved for Legal ID: {}", request.getLegalId());

            // Step 13: Log the successful completion
            currentStep = "COMPLETED";
            String successRemark = buildSuccessRemark(cif, khrAccount, usdAccount, mnemonic);
            reportLogService.createAccountOpeningLog(
                    request.getLegalId(),
                    OpenAccStatusEnum.SUCCESS,
                    successRemark,
                    null
            );
            log.info("Account opening completed successfully for Legal ID: {}", request.getLegalId());

            reportLogService.saveLogReport(
                    request.getLegalId(),
                    OpenAccStatusEnum.SUCCESS,
                    "Open account online Successfully"
            );

            // Step 14: Return success response
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

    @Override
    @Transactional
    public CustomerResponse openAccountMock(CustomerRequest request) {
        log.info("[MOCK] Processing account opening for Legal ID: {}", request.getLegalId());

        String currentStep = "START";
        String cif = null;
        String mnemonic = null;
        String khrAccount = null;
        String usdAccount = null;

        try {
            // Optional validations still run but do not call T24
            currentStep = "CHECK_DATABASE_CONNECTIONS";
            try { validationService.checkDatabaseConnections(); } catch (Exception e) { log.warn("[MOCK] DB check warning: {}", e.getMessage()); }

            currentStep = "GET_CUSTOMER_INFO";
            Map<String, String> customerInfo = new HashMap<>();

            // Generate MOCK data
            currentStep = "CREATE_CUSTOMER";
            cif = "MOCKCIF_" + System.currentTimeMillis();
            mnemonic = "MOCKMN_" + (int)(Math.random() * 100000);

            // Create MOCK accounts
            currentStep = "CREATE_KHR_ACCOUNT";
            khrAccount = "MOCK-KHR-" + (10000000 + (int)(Math.random() * 89999999));

            currentStep = "CREATE_USD_ACCOUNT";
            usdAccount = "MOCK-USD-" + (10000000 + (int)(Math.random() * 89999999));

            // Non-blocking mock mobile banking activation
            currentStep = "ACTIVATE_MOBILE_BANKING";
            try { mobileBankingService.activate(request, cif, khrAccount, usdAccount); } catch (Exception e) { log.warn("[MOCK] Mobile banking skipped: {}", e.getMessage()); }

            // Create AML record and notify (real persistence + email)
            currentStep = "CREATE_AML_AND_NOTIFY";
            try { createAmlRecordAndNotify(request, cif, khrAccount, usdAccount, mnemonic); } catch (Exception e) { log.warn("[MOCK] AML notify warning: {}", e.getMessage()); }

            // Save customer images using provided base64
            currentStep = "SAVE_CUSTOMER_IMAGES";
            CustomerImageUploadResponseDto imagePaths = null;
            try {
                CustomerFileUploadRequestDto fileRequest = CustomerFileUploadRequestDto.builder()
                        .legal_id(request.getLegalId())
                        .NidImage(request.getNidImage())
                        .SelfieImage(request.getSelfieImage())
                        .build();
                imagePaths = customerImageService.saveCustomerImages(fileRequest);
            } catch (Exception e) { log.warn("[MOCK] Save images warning: {}", e.getMessage()); }

            // Save success log (real)
            currentStep = "SAVE_SUCCESS_LOG";
            try { accountOnlineOpenSuccessService.saveSuccessLog(request, imagePaths); } catch (Exception e) { log.warn("[MOCK] Save success log warning: {}", e.getMessage()); }

            // Report logs (real)
            currentStep = "COMPLETED";
            String successRemark = buildSuccessRemark(cif, khrAccount, usdAccount, mnemonic);
            try {
                reportLogService.createAccountOpeningLog(
                        request.getLegalId(),
                        OpenAccStatusEnum.SUCCESS,
                        successRemark,
                        null
                );
                reportLogService.saveLogReport(request.getLegalId(), OpenAccStatusEnum.SUCCESS, "Open account online Successfully (MOCK)");
            } catch (Exception e) {
                log.warn("[MOCK] Save report log warning: {}", e.getMessage());
            }

            return CustomerResponse.builder()
                    .cif(cif)
                    .khrAccount(khrAccount)
                    .usdAccount(usdAccount)
                    .mnemonic(mnemonic)
                    .build();

        } catch (Exception e) {
            String failureRemark = buildFailureRemark(currentStep, cif, khrAccount, usdAccount);
            try {
                reportLogService.createAccountOpeningLog(
                        request.getLegalId(),
                        OpenAccStatusEnum.FAILURE,
                        failureRemark,
                        e
                );
            } catch (Exception ex) { log.warn("[MOCK] Failed to save failure log: {}", ex.getMessage()); }
            throw e;
        }
    }

    /**
     * Creates AML record in PENDING status and sends email notification
     */
    private void createAmlRecordAndNotify(CustomerRequest request, String cif,
                                          String khrAccount, String usdAccount, String mnemonic) {
        try {
            // Build CustomerAmlDto
            CustomerAmlDto customerAmlDto = CustomerAmlDto.builder()
                    .givenName(request.getGivenName())
                    .idDisplay(request.getLegalId())
                    .familyName(request.getFamilyName())
                    .firstNameKh(request.getFirstNameKh())
                    .lastNameKh(request.getLastNameKh())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .nationality(request.getNationality())
                    .legalAddress(request.getLegalAddress())
                    .build();

            String requestJson = objectMapper.writeValueAsString(customerAmlDto);

            // Build response object
            CustomerResponse response = CustomerResponse.builder()
                    .cif(cif)
                    .khrAccount(khrAccount)
                    .usdAccount(usdAccount)
                    .mnemonic(mnemonic)
                    .build();

            String responseJson = objectMapper.writeValueAsString(response);

            // Create AML request
            CreateAmlRequestDto amlRequest = CreateAmlRequestDto.builder()
                    .originalRequest(requestJson)
                    .originalResponse(responseJson)
                    .status(AmlStatusEnum.PENDING)
                    .idDisplay(request.getLegalId())
                    .familyName(request.getFamilyName())
                    .givenName(request.getGivenName())
                    .firstNameKh(request.getFirstNameKh())
                    .lastNameKh(request.getLastNameKh())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .nationality(request.getNationality())
                    .legalAddress(request.getLegalAddress())
                    .build();

            // Create AML status record
            var amlStatus = amlService.createAmlStatus(amlRequest);
            log.info("AML record created in PENDING status. ID: {}", amlStatus.getId());

            // Build AML status DTO for email
            AmlStatusDto amlStatusDto = AmlStatusDto.builder()
                    .status(AmlStatusEnum.PENDING)
                    .approvedBy(null)
                    .rejectedBy(null)
                    .originalRequest(requestJson)
                    .originalResponse(responseJson)
                    .customerInfo(customerAmlDto)
                    .build();

            // Send email notification (asynchronous - non-blocking)
            try {
                mailService.sendAmlStatusNotification(amlStatusDto);
                log.info("AML status notification email sent for Legal ID: {}", request.getLegalId());
            } catch (Exception e) {
                log.warn("Failed to send AML status email for Legal ID {}: {}",
                        request.getLegalId(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error in createAmlRecordAndNotify for Legal ID {}: {}",
                    request.getLegalId(), e.getMessage());
            throw new RuntimeException("Failed to create AML record and send notification", e);
        }
    }

    @Override
    @Transactional
    public CustomerResponse testAmlFlow() {
        try {
            // Step 1: Generate dynamic mock customer info
            String randomId = "LEGAL_" + System.currentTimeMillis();
            String familyName = "Family" + (int) (Math.random() * 1000);
            String givenName = "Given" + (int) (Math.random() * 1000);
            String firstNameKh = "ភា" + (int) (Math.random() * 1000);
            String lastNameKh = "ន" + (int) (Math.random() * 1000);
            String dateOfBirth = "1990-" + (1 + (int) (Math.random() * 12)) + "-" + (1 + (int) (Math.random() * 28));
            String gender = Math.random() > 0.5 ? "Male" : "Female";
            String nationality = "Cambodian";
            String legalAddress = "Street " + (int) (Math.random() * 200) + ", Phnom Penh";
            String phoneNumber = "0" + (10000000 + (int) (Math.random() * 89999999));
            String recId = "REC_" + System.currentTimeMillis();

            log.info("🚀 Starting AML full test flow for Legal ID: {}", randomId);

            // Step 2: Load mock images with fallback support
            ClassPathResource nidResource = new ClassPathResource("mock/mock_nid.jpg");
            byte[] nidBytes;
            try (InputStream in = nidResource.getInputStream();
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = in.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                nidBytes = buffer.toByteArray();
                log.info("✅ Mock NID image loaded successfully ({} bytes)", nidBytes.length);
            }

            // Try loading selfie image with multiple fallbacks
            byte[] selfieBytes = null;
            String[] selfieAttempts = {"mock/mock_selfie.jpg", "mock/mock_selfie.png", "mock/mock_nid.jpg"};

            for (String path : selfieAttempts) {
                try {
                    ClassPathResource selfieResource = new ClassPathResource(path);
                    try (InputStream in = selfieResource.getInputStream();
                         ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                        int nRead;
                        byte[] data = new byte[1024];
                        while ((nRead = in.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        selfieBytes = buffer.toByteArray();
                        log.info("✅ Mock selfie image loaded from: {} ({} bytes)", path, selfieBytes.length);
                        break;
                    }
                } catch (Exception e) {
                    log.debug("⚠️ Could not load from {}: {}", path, e.getMessage());
                }
            }

            // Fallback to NID if selfie not found
            if (selfieBytes == null) {
                selfieBytes = nidBytes;
                log.warn("⚠️ Using NID image as selfie fallback");
            }

            // Convert to base64 with proper MIME type detection
            String nidMimeType = "image/jpeg";
            String selfieMimeType = "image/jpeg";

            String dummyNidBase64 = "data:" + nidMimeType + ";base64," +
                    java.util.Base64.getEncoder().encodeToString(nidBytes);
            String dummySelfieBase64 = "data:" + selfieMimeType + ";base64," +
                    java.util.Base64.getEncoder().encodeToString(selfieBytes);

            log.info("📸 Base64 images prepared - NID: {} bytes, Selfie: {} bytes",
                    nidBytes.length, selfieBytes.length);

            // Step 3: Build dummy customer request
            CustomerRequest request = CustomerRequest.builder()
                    .recId(recId)
                    .legalId(randomId)
                    .familyName(familyName)
                    .givenName(givenName)
                    .firstNameKh(firstNameKh)
                    .lastNameKh(lastNameKh)
                    .dateOfBirth(dateOfBirth)
                    .gender(gender)
                    .nationality(nationality)
                    .legalAddress(legalAddress)
                    .phoneNumber(phoneNumber)
                    .nidImage(dummyNidBase64)
                    .selfieImage(dummySelfieBase64)
                    .build();

            // Step 4: Prepare AML request and response
            CustomerAmlDto customerAmlDto = CustomerAmlDto.builder()
                    .givenName(givenName)
                    .idDisplay(randomId)
                    .familyName(familyName)
                    .firstNameKh(firstNameKh)
                    .lastNameKh(lastNameKh)
                    .dateOfBirth(dateOfBirth)
                    .gender(gender)
                    .nationality(nationality)
                    .legalAddress(legalAddress)
                    .build();

            String requestJson = objectMapper.writeValueAsString(customerAmlDto);

            CustomerResponse dummyResponse = CustomerResponse.builder()
                    .cif("TEST_CIF_" + System.currentTimeMillis())
                    .khrAccount(null)
                    .usdAccount(null)
                    .mnemonic("AML_TEST_" + System.currentTimeMillis())
                    .build();

            String responseJson = objectMapper.writeValueAsString(dummyResponse);

            CreateAmlRequestDto pendingRequest = CreateAmlRequestDto.builder()
                    .originalRequest(requestJson)
                    .originalResponse(responseJson)
                    .status(AmlStatusEnum.PENDING)
                    .idDisplay(randomId)
                    .familyName(familyName)
                    .givenName(givenName)
                    .firstNameKh(firstNameKh)
                    .lastNameKh(lastNameKh)
                    .dateOfBirth(dateOfBirth)
                    .gender(gender)
                    .nationality(nationality)
                    .legalAddress(legalAddress)
                    .build();

            AmlStatusDto amlStatusDto = AmlStatusDto.builder()
                    .status(AmlStatusEnum.PENDING)
                    .approvedBy(null)
                    .rejectedBy(null)
                    .originalRequest(requestJson)
                    .originalResponse(responseJson)
                    .customerInfo(customerAmlDto)
                    .build();

            // Step 5: Create AML record in PENDING status
            var amlPending = amlService.createAmlStatus(pendingRequest);
            log.info("✅ AML record created in PENDING status. ID: {}", amlPending.getId());

            // Step 6: Save mock customer images (locally and via service)
            CustomerImageUploadResponseDto imagePaths = null;
            try {
                // Save locally under /mock_images/
                java.nio.file.Path mockDir = java.nio.file.Paths.get("mock_images");
                java.nio.file.Files.createDirectories(mockDir);

                String nidFileName = randomId + "_nid.jpg";
                String selfieFileName = randomId + "_selfie.jpg";

                java.nio.file.Files.write(mockDir.resolve(nidFileName), nidBytes);
                java.nio.file.Files.write(mockDir.resolve(selfieFileName), selfieBytes);

                log.info("💾 Mock images saved locally:");
                log.info("   - NID: {}", mockDir.resolve(nidFileName).toAbsolutePath());
                log.info("   - Selfie: {}", mockDir.resolve(selfieFileName).toAbsolutePath());

                // Save through image service (base64 upload)
                CustomerFileUploadRequestDto fileRequest = CustomerFileUploadRequestDto.builder()
                        .legal_id(request.getLegalId())
                        .NidImage(request.getNidImage())
                        .SelfieImage(request.getSelfieImage())
                        .build();

                imagePaths = customerImageService.saveCustomerImages(fileRequest);
                log.info("✅ Customer images saved via service:");
                log.info("   - NID Path: {}", imagePaths.getNidImagePath());
                log.info("   - Selfie Path: {}", imagePaths.getSelfieImagePath());

            } catch (Exception e) {
                log.error("⚠️ Failed to save mock images for test AML flow (Legal ID: {}): {}",
                        request.getLegalId(), e.getMessage(), e);
            }

            // Step 7: Save success log safely
            try {
                accountOnlineOpenSuccessService.saveSuccessLog(request, imagePaths);
                log.info("✅ Success log saved for Legal ID: {}", request.getLegalId());
            } catch (Exception e) {
                log.warn("⚠️ Failed to save success log for Legal ID {}: {}", request.getLegalId(), e.getMessage());
            }

            // Step 8: Send AML status email asynchronously
            try {
                mailService.sendAmlStatusNotification(amlStatusDto);
                log.info("📧 AML status notification email sent for test flow");
            } catch (Exception e) {
                log.warn("⚠️ Failed to send AML status email for ID {}: {}", randomId, e.getMessage());
            }

            // Step 9: Return dummy response
            log.info("✅ Test AML flow completed successfully for Legal ID: {}", randomId);
            return dummyResponse;

        } catch (Exception e) {
            log.error("❌ AML full test flow failed - Error: {}", e.getMessage(), e);
            throw new RuntimeException("AML full test flow failed", e);
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