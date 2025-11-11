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
            log.info("Processing account opening for Legal ID: {}", request.getLegalId());

            String currentStep = "START";
            String cif = null;
            String mnemonic = null;
            String khrAccount = null;
            String usdAccount = null;

            try {
                // Step 1: Check database connections
    //            currentStep = "CHECK_DATABASE_CONNECTIONS";
    //            validationService.checkDatabaseConnections();
    //            log.info("Database connections verified for Legal ID: {}", request.getLegalId());

                // Step 2: Get customer info
                currentStep = "GET_CUSTOMER_INFO";
                Map<String, String> customerInfo = validationService.getCustomerInfo(request.getLegalId());
                log.info("Customer info retrieved for Legal ID: {}", request.getLegalId());

                // Step 3: Validate customer rating
    //            currentStep = "VALIDATE_CUSTOMER_RATING";
    //            validationService.validateCustomerRating(customerInfo);
    //            log.info("Customer rating validated for Legal ID: {}", request.getLegalId());

                // Step 4: Validate existing accounts
                currentStep = "VALIDATE_EXISTING_ACCOUNTS";
                validationService.validateExistingAccounts(customerInfo);
                log.info("Existing accounts validated for Legal ID: {}", request.getLegalId());

                // Step 5: Create customer if needed
                currentStep = "CREATE_CUSTOMER";
                cif = customerInfo.get("CIF");

                if (cif == null || cif.isEmpty()) {
                    // Call T24 service to create customer
                    Document t24Response = t24Service.createCustomer(request);

                    if (t24Response == null) {
                        log.error("Step 5 - CREATE_CUSTOMER: T24 returned null response for Legal ID: {}", request.getLegalId());
                        throw new AccountCreationException("T24 returned null response for customer creation");
                    }

                    // Log if T24 returned an error
                    if (XmlParser.hasError(t24Response)) {
                        String errorMsg = XmlParser.extractErrorMessage(t24Response);
                        log.error("Step 5 - CREATE_CUSTOMER: T24 returned error for Legal ID {}: {}", request.getLegalId(), errorMsg);
                    } else {
                        log.info("Step 5 - CREATE_CUSTOMER: T24 response received successfully for Legal ID: {}", request.getLegalId());
                    }

                    // Extract CIF and MNEMONIC using XmlParser
                    cif = XmlParser.extractCif(t24Response);
                    mnemonic = XmlParser.extractMnemonic(t24Response);

                    if (cif == null || cif.isEmpty()) {
                        log.warn("Step 5 - CREATE_CUSTOMER: No CIF returned from T24 for Legal ID: {}", request.getLegalId());
                        throw new AccountCreationException("No CIF returned from T24");
                    } else {
                        log.info("Step 5 - CREATE_CUSTOMER: New customer created - CIF: {}, MNEMONIC: {} for Legal ID: {}",
                                cif, mnemonic, request.getLegalId());
                    }
                } else {
                    log.info("Step 5 - CREATE_CUSTOMER: Using existing CIF: {} for Legal ID: {}", cif, request.getLegalId());
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