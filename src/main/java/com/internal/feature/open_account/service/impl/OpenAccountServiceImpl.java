package com.internal.feature.open_account.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AmlStatusEnum;
import com.internal.enumation.OpenAccStatusEnum;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

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
    private final ObjectMapper objectMapper;

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
            log.info(">>> Step 2: GET_CUSTOMER_INFO");
            Map<String, String> customerInfo = validationService.getCustomerInfo(request.getLegalId());
            log.info("Customer info retrieved: {}", customerInfo != null ? "Found" : "Not found");

            // Step 4: Validate existing accounts
            currentStep = "VALIDATE_EXISTING_ACCOUNTS";
            log.info(">>> Step 4: VALIDATE_EXISTING_ACCOUNTS");
            validationService.validateExistingAccounts(customerInfo);
            log.info("Existing accounts validation passed");

            // Step 5: Create customer if needed
            currentStep = "CREATE_CUSTOMER";
            log.info(">>> Step 5: CREATE_CUSTOMER");
            cif = customerInfo.get("CIF");

            if (cif == null || cif.isEmpty()) {
                log.info("No existing CIF found. Creating new customer in T24...");
                log.debug("Customer details - Given Name: {}, Family Name: {}, DOB: {}, Gender: {}, Nationality: {}",
                        request.getGivenName(), request.getFamilyName(),
                        request.getDateOfBirth(), request.getGender(), request.getNationality());

                // Call T24 service to create customer
                Document t24Response = t24Service.createCustomer(request);

                if (t24Response == null) {
                    log.error("Step 5 FAILED: T24 returned null response");
                    throw new AccountCreationException("T24 returned null response for customer creation");
                }

                // Check for T24 errors
                if (XmlParser.hasError(t24Response)) {
                    String errorMsg = XmlParser.extractErrorMessage(t24Response);
                    log.error("Step 5 FAILED: T24 returned error: {}", errorMsg);
                    throw new AccountCreationException("T24 error: " + errorMsg);
                }

                // Extract CIF and MNEMONIC
                cif = XmlParser.extractCif(t24Response);
                mnemonic = XmlParser.extractMnemonic(t24Response);

                if (cif == null || cif.isEmpty()) {
                    log.error("Step 5 FAILED: No CIF extracted from T24 response");
                    throw new AccountCreationException("No CIF returned from T24");
                }

                log.info("Step 5 SUCCESS: Customer created in T24");
                log.info("  - CIF: {}", cif);
                log.info("  - MNEMONIC: {}", mnemonic);

            } else {
                log.info("Existing CIF found: {}", cif);
                log.info("Step 5 SKIPPED: Using existing customer");
            }

            // Step 6: Create KHR account
            if (!validationService.hasAccount(customerInfo, "KHR")) {
                currentStep = "CREATE_KHR_ACCOUNT";
                log.info(">>> Step 6: CREATE_KHR_ACCOUNT");
                khrAccount = createAccount(request, cif, "KHR");
                if (khrAccount != null) {
                    log.info("Step 6 SUCCESS: KHR account created: {}", khrAccount);
                } else {
                    log.warn("Step 6 FAILED: KHR account creation returned null");
                }
            } else {
                log.info(">>> Step 6: CREATE_KHR_ACCOUNT - SKIPPED (already exists)");
            }

            // Step 7: Create USD account
            if (!validationService.hasAccount(customerInfo, "USD")) {
                currentStep = "CREATE_USD_ACCOUNT";
                log.info(">>> Step 7: CREATE_USD_ACCOUNT");
                usdAccount = createAccount(request, cif, "USD");
                if (usdAccount != null) {
                    log.info("Step 7 SUCCESS: USD account created: {}", usdAccount);
                } else {
                    log.warn("Step 7 FAILED: USD account creation returned null");
                }
            } else {
                log.info(">>> Step 7: CREATE_USD_ACCOUNT - SKIPPED (already exists)");
            }

            // Step 8: Validate at least one account created
            currentStep = "VALIDATE_ACCOUNT_CREATION";
            log.info(">>> Step 8: VALIDATE_ACCOUNT_CREATION");
            if (khrAccount == null && usdAccount == null &&
                    !validationService.hasAccount(customerInfo, "KHR") &&
                    !validationService.hasAccount(customerInfo, "USD")) {
                log.error("Step 8 FAILED: No accounts created or exist");
                throw new AccountCreationException(AppConstants.FAIL_CREATE_ANY_ACCOUNT);
            }
            log.info("Step 8 SUCCESS: At least one account exists");

            // Step 9: Activate mobile banking (non-blocking)
            currentStep = "ACTIVATE_MOBILE_BANKING";
            log.info(">>> Step 9: ACTIVATE_MOBILE_BANKING");
            try {
                mobileBankingService.activate(request, cif, khrAccount, usdAccount);
                log.info("Step 9 SUCCESS: Mobile banking activated");
            } catch (Exception e) {
                log.warn("Step 9 WARNING: Mobile banking activation failed (non-critical): {}", e.getMessage());
            }

            // Step 10: Create AML record and send notification
            currentStep = "CREATE_AML_AND_NOTIFY";
            log.info(">>> Step 10: CREATE_AML_AND_NOTIFY");
            try {
                createAmlRecordAndNotify(request, cif, khrAccount, usdAccount, mnemonic);
                log.info("Step 10 SUCCESS: AML record created and notification sent");
            } catch (Exception e) {
                log.error("Step 10 WARNING: Failed to create AML record or send notification: {}", e.getMessage());
            }

            // Step 11: Save customer images
            currentStep = "SAVE_CUSTOMER_IMAGES";
            log.info(">>> Step 11: SAVE_CUSTOMER_IMAGES");
            CustomerFileUploadRequestDto fileRequest = CustomerFileUploadRequestDto.builder()
                    .legal_id(request.getLegalId())
                    .NidImage(request.getNidImage())
                    .SelfieImage(request.getSelfieImage())
                    .build();

            CustomerImageUploadResponseDto imagePaths = customerImageService.saveCustomerImages(fileRequest);
            log.info("Step 11 SUCCESS: Images saved - NID: {}, Selfie: {}",
                    imagePaths.getNidImagePath(), imagePaths.getSelfieImagePath());

            // Step 12: Save success log
            currentStep = "SAVE_SUCCESS_LOG";
            log.info(">>> Step 12: SAVE_SUCCESS_LOG");
            accountOnlineOpenSuccessService.saveSuccessLog(request, imagePaths);
            log.info("Step 12 SUCCESS: Success log saved");

            // Step 13: Log the successful completion
            currentStep = "COMPLETED";
            String successRemark = buildSuccessRemark(cif, khrAccount, usdAccount, mnemonic);
            reportLogService.createAccountOpeningLog(
                    request.getLegalId(),
                    OpenAccStatusEnum.SUCCESS,
                    successRemark,
                    null
            );

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
            return CustomerResponse.builder()
                    .cif(cif)
                    .khrAccount(khrAccount)
                    .usdAccount(usdAccount)
                    .mnemonic(mnemonic)
                    .build();

        } catch (Exception e) {
            log.error("========== ACCOUNT OPENING FAILED ==========");
            log.error("Failed at step: {}", currentStep);
            log.error("Legal ID: {}", request.getLegalId());
            log.error("Error: {}", e.getMessage());
            log.error("CIF: {}", cif);
            log.error("KHR Account: {}", khrAccount);
            log.error("USD Account: {}", usdAccount);
            log.error("============================================");

            // Log the failure
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
            log.info("AML record created - ID: {}, Status: PENDING", amlStatus.getId());

            // Build AML status DTO for email
            AmlStatusDto amlStatusDto = AmlStatusDto.builder()
                    .status(AmlStatusEnum.PENDING)
                    .approvedBy(null)
                    .rejectedBy(null)
                    .originalRequest(requestJson)
                    .originalResponse(responseJson)
                    .customerInfo(customerAmlDto)
                    .build();

            // Send email notification (asynchronous)
            try {
                mailService.sendAmlStatusNotification(amlStatusDto);
                log.info("AML notification email sent");
            } catch (Exception e) {
                log.warn("Failed to send AML notification email: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error in createAmlRecordAndNotify: {}", e.getMessage());
            throw new RuntimeException("Failed to create AML record and send notification", e);
        }
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
}