package com.internal.feature.open_account.service.impl;

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
import com.internal.feature.logs_report.service.AccountOnlineOpenSuccessService;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.logs_report.service.CustomerImageService;
import com.internal.feature.mail.service.MailService;
import com.internal.feature.open_account.dto.request.CustomerAmlRequest;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.AmlResponseDto;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.feature.open_account.service.external.*;
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
    private final AmlMiddlewareService amlMiddlewareService;

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

            // Step 3 : Process AML
            currentStep = "PROCESS_AML";
            log.info(">>> Step 3: PROCESS_AML");
            try {
                createAmlRecordAndNotify(request, cif, khrAccount, usdAccount, mnemonic);
                log.info("Step 3 SUCCESS: PROCESS AML SuccessFully");
            } catch (Exception e) {
                log.error("Step 3 WARNING: Failed to process AML: {}", e.getMessage());
            }

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
     * process AML
     */
    private void createAmlRecordAndNotify(CustomerRequest request, String cif,
                                          String khrAccount, String usdAccount, String mnemonic) {
        try {
            // 1. Check if AML record already exists
            var existingAml = amlService.findByLegalId(request.getLegalId());
            if (existingAml.isPresent()) {
                AmlStatus existing = existingAml.get();
                AmlStatusEnum status = existing.getStatus();

                switch (status) {
                    case PENDING:
                        throw new AccountCreationException("AML process already pending for this legal ID: " + request.getLegalId());
                    case APPROVE:
                        throw new AccountCreationException("AML already approved for this legal ID: " + request.getLegalId());
                    case REJECT:
                        throw new AccountCreationException("AML already rejected for this legal ID: " + request.getLegalId());
                    default:
                        throw new AccountCreationException("AML record already exists for this legal ID: " + request.getLegalId());
                }
            }

            // 2. Map CustomerRequest to CustomerAmlRequest
            CustomerAmlRequest amlRequestDto = CustomerAmlRequest.builder()
                    .customerId(request.getLegalId())
                    .custCreateDate(request.getLegalIssueDate())
                    .customerType("Active")
                    .custName(request.getFamilyName() + request.getGivenName())
                    .givenName(request.getGivenName())
                    .familyName(request.getFamilyName())
                    .gender(request.getGender())
                    .dateOfBirth(request.getDateOfBirth())
                    .nationality(request.getNationality())
                    .legalAddress(request.getLegalAddress())
                    .custDistrict(request.getCustomerDistrict())
                    .custProvince(request.getCustomerProvince())
                    .phoneNumber(request.getPhoneNumber())
                    .occupation(request.getOccupation())
                    .legalId(request.getLegalId())
                    .maritalStatus(request.getMaritalStatus())
                    .target("220")
                    .legalDocName(request.getLegalDocName())
                    .legalExpDate(request.getLegalExpireDate())
                    .nidImage(request.getNidImage())
                    .selfieImage(request.getSelfieImage())
                    .build();

            // 3. Call AML middleware
            AmlResponseDto amlResponse = amlMiddlewareService.CheckAml(amlRequestDto);
            log.info("AML Middleware response: RiskLevel={}, TrxnID={}", amlResponse.getRiskLevel(), amlResponse.getTrxnID());

            // 4. Check RiskLevel
            if ("High".equalsIgnoreCase(amlResponse.getRiskLevel())) {
                throw new AccountCreationException("AML risk level is HIGH. Manual review required for legal ID: " + request.getLegalId());
            }

            // 5. Build response for AML record
            CustomerResponse response = CustomerResponse.builder()
                    .cif(cif)
                    .khrAccount(khrAccount)
                    .usdAccount(usdAccount)
                    .mnemonic(mnemonic)
                    .build();

            // 6. Save AML request record
            CreateAmlRequestDto createAmlRequest = CreateAmlRequestDto.builder()
                    .originalRequest(objectMapper.writeValueAsString(amlRequestDto))
                    .originalResponse(objectMapper.writeValueAsString(response))
                    .status(AmlStatusEnum.PENDING)
                    .legalId(request.getLegalId())
                    .familyName(request.getFamilyName())
                    .givenName(request.getGivenName())
                    .firstNameKh(request.getFirstNameKh())
                    .lastNameKh(request.getLastNameKh())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .nationality(request.getNationality())
                    .legalAddress(request.getLegalAddress())
                    .screeningResult(objectMapper.writeValueAsString(amlResponse))
                    .build();

            var amlStatus = amlService.createAmlStatus(createAmlRequest);
            log.info("AML record created - ID: {}, Status: PENDING", amlStatus.getId());

            // 7. Send AML notification email
            AmlStatusDto amlStatusDto = AmlStatusDto.builder()
                    .status(AmlStatusEnum.PENDING)
                    .approvedBy(null)
                    .rejectedBy(null)
                    .originalRequest(objectMapper.writeValueAsString(amlRequestDto))
                    .originalResponse(objectMapper.writeValueAsString(response))
                    .customerInfo(CustomerAmlDto.builder()
                            .givenName(request.getGivenName())
                            .familyName(request.getFamilyName())
                            .firstNameKh(request.getFirstNameKh())
                            .lastNameKh(request.getLastNameKh())
                            .dateOfBirth(request.getDateOfBirth())
                            .gender(request.getGender())
                            .nationality(request.getNationality())
                            .legalAddress(request.getLegalAddress())
                            .build())
                    .build();

            try {
                mailService.sendAmlStatusNotification(amlStatusDto);
                log.info("AML notification email sent");
            } catch (Exception e) {
                log.warn("Failed to send AML notification email: {}", e.getMessage());
            }

        } catch (AccountCreationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in createAmlRecordAndNotify: {}", e.getMessage(), e);
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