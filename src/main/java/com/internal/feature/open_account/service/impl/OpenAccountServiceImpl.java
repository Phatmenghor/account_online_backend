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
import com.internal.feature.logs_report.service.AccountOnlineOpenSuccessService;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.logs_report.service.CustomerImageService;
import com.internal.feature.mail.service.MailService;
import com.internal.feature.open_account.dto.request.CustomerAmlRequest;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.AmlExternalResponseDto;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.feature.open_account.service.external.*;
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
            Map<String, String> customerInfo = getCustomerInfo(request);

            // Step 3: Process AML
            currentStep = "PROCESS_AML";
            processAml(request, cif, khrAccount, usdAccount, mnemonic);

            // Step 4: Validate existing accounts
            currentStep = "VALIDATE_EXISTING_ACCOUNTS";
            validateExistingAccounts(customerInfo);

            // Step 5: Create customer
            currentStep = "CREATE_CUSTOMER";
            cif = createCustomerIfNeeded(request, customerInfo);
            mnemonic = XmlParser.extractMnemonic(t24Service.createCustomer(request)); // optional refactor to return both

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

            // Step 11: Save customer images (non-blocking)
            currentStep = "SAVE_CUSTOMER_IMAGES";
            CustomerImageUploadResponseDto imagePaths = safeSaveCustomerImages(request);

            // Step 12: Save success log (non-blocking)
            currentStep = "SAVE_SUCCESS_LOG";
            safeSaveSuccessLog(request, imagePaths);

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

    private Map<String, String> getCustomerInfo(CustomerRequest request) {
        log.info(">>> Step 2: GET_CUSTOMER_INFO");
        Map<String, String> customerInfo = validationService.getCustomerInfo(request.getLegalId());
        log.info("Customer info retrieved: {}", customerInfo != null ? "Found" : "Not found");
        return customerInfo;
    }

    private void processAml(CustomerRequest request, String cif, String khrAccount, String usdAccount, String mnemonic) {
        log.info(">>> Step 3: PROCESS_AML");
        try {
            createAmlRecordAndNotify(request, cif, khrAccount, usdAccount, mnemonic);
            log.info("Step 3 SUCCESS: PROCESS AML Successfully");
        } catch (Exception e) {
            log.warn("Step 3 WARNING: Failed to process AML: {}", e.getMessage());
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

    private void safeSaveSuccessLog(CustomerRequest request, CustomerImageUploadResponseDto imagePaths) {
        log.info(">>> Step 12: SAVE_SUCCESS_LOG");
        try {
            accountOnlineOpenSuccessService.saveSuccessLog(request, imagePaths);
            log.info("Step 12 SUCCESS: Success log saved");
        } catch (Exception e) {
            log.warn("Step 12 WARNING: Failed to save success log (non-critical): {}", e.getMessage());
        }
    }

    /**
     * process AML
     */
    private void createAmlRecordAndNotify(CustomerRequest request, String cif,
                                          String khrAccount, String usdAccount, String mnemonic) {
        try {
            checkExistingAmlRecord(request.getLegalId());
            CustomerAmlRequest amlRequestDto = buildAmlRequestDto(request);
            AmlExternalResponseDto amlResponse = callAmlMiddleware(amlRequestDto, request.getLegalId());
            validateRiskLevel(amlResponse, request.getLegalId());

            CustomerResponse customerResponse = buildCustomerResponse(cif, khrAccount, usdAccount, mnemonic);
            AmlStatusDto amlStatus = saveAmlRecord(amlRequestDto, amlResponse, customerResponse, request);
            sendAmlNotification(amlRequestDto, amlResponse, request);

            log.info("Step 3 SUCCESS: AML record created - ID: {}", amlStatus.getId());
        } catch (AccountCreationException e) {
            throw e; // known business exception
        } catch (Exception e) {
            log.error("Error in AML processing: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create AML record or send notification", e);
        }
    }

    private CustomerAmlRequest buildAmlRequestDto(CustomerRequest request) {
        return CustomerAmlRequest.builder()
                .customerId(request.getLegalId()) // AML system ID
                .custCreateDate(request.getLegalIssueDate()) // must match format expected by AML
                .customerType("ACTIVE")
                .custName(request.getFamilyName() + " " + request.getGivenName()) // space between names
                .givenName(request.getGivenName())
                .familyName(request.getFamilyName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth()) // format yyyyMMdd
                .nationality("KH")
                .legalAddress(request.getLegalAddress() != null ? request.getLegalAddress() : "NA")
                .custDistrict(request.getCustomerPobDistrict())
                .custProvince(request.getCustomerPobProvince())
                .country("Cambodia") // can be null
                .sms1(null) // optional
                .phoneNumber(request.getPhoneNumber())
                .offPhone(null) // optional
                .occupation(request.getOccupation()) // can be null
                .legalId(request.getLegalId() + "-NATIONAL.ID") // must match AML format
                .maritalStatus(request.getMaritalStatus())
                .businessSector(null) // optional
                .target("220")
                .income(0) // must be integer
                .dobYear(null) // optional
                .dobMonth(null) // optional
                .dobDay(null) // optional
                .legalDocName("NATIONAL.ID")
                .legalExpDate(request.getLegalExpireDate()) // format yyyyMMdd
                .customerRating("1")
                .build();
    }

    private void checkExistingAmlRecord(String legalId) {
        Optional<AmlStatus> existingAml = amlService.findByLegalId(legalId);
        if (existingAml.isPresent()) {
            AmlStatus existing = existingAml.get();
            AmlStatusEnum status = existing.getStatus();

            String msg;
            switch (status) {
                case PENDING:
                    msg = "AML process already pending for this legal ID";
                    break;
                case APPROVE:
                    msg = "AML already approved for this legal ID";
                    break;
                case REJECT:
                    msg = "AML already rejected for this legal ID";
                    break;
                default:
                    msg = "AML record already exists for this legal ID";
                    break;
            }

            throw new AccountCreationException(msg + ": " + legalId);
        }
    }


    private AmlExternalResponseDto callAmlMiddleware(CustomerAmlRequest amlRequest, String legalId) throws JsonProcessingException {
        AmlExternalResponseDto response = amlMiddlewareService.CheckAml(amlRequest);
        log.info("AML Middleware response: RiskLevel={}, TrxnID={}", response.getRiskLevel(), response.getTrxnID());
        return response;
    }

    private void validateRiskLevel(AmlExternalResponseDto amlResponse, String legalId) {
        if ("High".equalsIgnoreCase(amlResponse.getRiskLevel())) {
            throw new AccountCreationException("AML risk level is HIGH. Manual review required for legal ID: " + legalId);
        }
    }

    private AmlStatusDto saveAmlRecord(CustomerAmlRequest amlRequest,
                                       AmlExternalResponseDto amlResponse,
                                       CustomerResponse customerResponse,
                                       CustomerRequest request) throws JsonProcessingException {

        CreateAmlRequestDto createRequest = CreateAmlRequestDto.builder()
                .originalRequest(objectMapper.writeValueAsString(amlRequest))
                .originalResponse(objectMapper.writeValueAsString(customerResponse))
                .status(AmlStatusEnum.PENDING)
                .legalId(request.getLegalId())
                .familyName(request.getFamilyName())
                .givenName(request.getGivenName())
                .firstNameKh(request.getFirstNameKh())
                .lastNameKh(request.getLastNameKh())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .nationality("KH")
                .customerCurrentProvince(request.getCustomerCurrentProvince())
                .customerCurrentDistrict(request.getCustomerCurrentDistrict())
                .customerCurrentCommune(request.getCustomerCurrentCommune())
                .customerCurrentVillage(request.getCustomerCurrentVillage())
                .customerPobProvince(request.getCustomerPobProvince())
                .customerPobDistrict(request.getCustomerPobDistrict())
                .customerPobCommune(request.getCustomerPobCommune())
                .customerPobVillage(request.getCustomerPobVillage())
                .legalAddress(request.getLegalAddress())
                .screeningResult(objectMapper.writeValueAsString(amlResponse))
                .RiskLevel(amlResponse.getRiskLevel())
                .ServiceName(amlResponse.getServiceName())
                .RulesTriggered(amlResponse.getRulesTriggered())
                .TrxnID(amlResponse.getTrxnID())
                .TotalRulesScore(amlResponse.getTotalRulesScore())
                .ActionTaken(amlResponse.getActionTaken())
                .build();

        return amlService.createAmlStatus(createRequest);
    }


    private void sendAmlNotification(CustomerAmlRequest amlRequest, AmlExternalResponseDto amlResponse, CustomerRequest request) {
        try {
            AmlStatusDto dto = AmlStatusDto.builder()
                    .status(AmlStatusEnum.PENDING)
                    .customerInfo(CustomerAmlDto.builder()
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
                    .originalRequest(asJson(amlRequest))
                    .originalResponse(asJson(amlResponse))
                    .build();

            mailService.sendAmlStatusNotification(dto);
            log.info("AML notification email sent");
        } catch (Exception e) {
            log.warn("Failed to send AML notification email: {}", e.getMessage());
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

    private CustomerResponse buildCustomerResponse(String cif, String khrAccount, String usdAccount, String mnemonic) {
        return CustomerResponse.builder()
                .cif(cif)
                .khrAccount(khrAccount)
                .usdAccount(usdAccount)
                .mnemonic(mnemonic)
                .build();
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