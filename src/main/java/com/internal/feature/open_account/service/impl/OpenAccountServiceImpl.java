package com.internal.feature.open_account.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AccountOpeningRequestStatusEnum;
import com.internal.enumation.AmlStatusEnum;
import com.internal.exceptions.error.custom.BusinessException;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.open_account.dto.OpenAccountContext;
import com.internal.feature.open_account.dto.request.ApproveAccountOpeningRequestDto;
import com.internal.feature.open_account.dto.request.CustomerCreationResult;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.request.RejectAccountOpeningRequestDto;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.dto.response.PendingAccountOpeningRequestDto;
import com.internal.feature.open_account.event.AccountOpenedEvent;
import com.internal.feature.open_account.facade.BankingService;
import com.internal.feature.open_account.facade.ComplianceService;
import com.internal.feature.open_account.facade.ReportingService;
import com.internal.feature.open_account.models.PendingAccountOpeningRequest;
import com.internal.feature.open_account.repository.PendingAccountOpeningRequestRepository;
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.feature.telegram_alerts.service.MonitoringService;
import com.internal.utils.SecurityUtils;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountServiceImpl implements OpenAccountService {

    private final BankingService bankingService;
    private final ComplianceService complianceService;
    private final ReportingService reportingService;
    private final ApplicationEventPublisher eventPublisher;
    private final MonitoringService monitoringService;
    private final PendingAccountOpeningRequestRepository pendingRequestRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CustomerResponse openAccount(CustomerRequest request) throws Exception {
        log.info("========== ACCOUNT OPENING STARTED ==========");
        log.info("Legal ID: {}", request.getLegalId());

        // Monitor: Account opening started
        monitoringService.logAccountOpeningStarted(
                request.getLegalId(),
                request.getNidImageName(),
                request.getSelfieImageName());

        OpenAccountContext context = OpenAccountContext.builder().request(request).build();
        String currentStep = "START";
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Test connection
            log.info(">>> Step 1: TEST_CONNECTION");
            currentStep = AppConstants.TEST_CONNECTION;
            bankingService.testConnection();
            log.info("Step 1 ✓ SUCCESS: Database connection is healthy");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "TEST_CONNECTION", true, "Banking service connection verified");

            // Step 2: Check existing complete account (recovery path for failed activation)
            log.info(">>> Step 2: CHECK_EXISTING_COMPLETE_ACCOUNT");
            currentStep = "CHECK_EXISTING_COMPLETE_ACCOUNT";
            var recoveryResult = bankingService.checkExistingCompleteAccountAndActivate(request);
            if (recoveryResult.isPresent()) {
                long totalDuration = System.currentTimeMillis() - startTime;
                log.info("Step 2 ✓ SUCCESS: Account recovery completed");
                log.info("========== ACCOUNT OPENING COMPLETED (RECOVERY) ==========");
                log.info("✓ Account already exists and is complete ({}ms)", totalDuration);
                // Build and return full response with account details
                var existingAccount = bankingService.getExistingAccountDetails(request.getLegalId());
                if (existingAccount.isPresent()) {
                    log.info("  • CIF: {} | Mnemonic: {}", existingAccount.get().getCif(), existingAccount.get().getMnemonic());
                    log.info("  • KHR Account: {}", existingAccount.get().getKhrAccount());
                    log.info("  • USD Account: {}", existingAccount.get().getUsdAccount());
                    return complianceService.buildCustomerAccInfo(
                            existingAccount.get().getCif(),
                            existingAccount.get().getKhrAccount(),
                            existingAccount.get().getUsdAccount(),
                            existingAccount.get().getMnemonic());
                }
            }
            log.info("Step 2 ✓ INFO: No existing complete account found - continuing normal flow");

            // Step 3: Customer matching (continue normal flow)
            log.info(">>> Step 3: GET_CUSTOMER_INFO");
            currentStep = AppConstants.GET_CUSTOMER_INFO;
            context.setCustomerInfo(bankingService.getCustomerInfo(request.getLegalId()));
            String customerCif = context.getCustomerInfo() != null ? context.getCustomerInfo().get("CIF") : "N/A";
            log.info("Step 3 ✓ SUCCESS: Customer info retrieved");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "GET_CUSTOMER_INFO", true, "Customer found with CIF: " + customerCif);

            // Step 4: Validation
            log.info(">>> Step 4: VALIDATE_EXISTING_ACCOUNTS");
            currentStep = AppConstants.VALIDATE_EXISTING_ACCOUNT;
            bankingService.validateExistingAccounts(context.getCustomerInfo());
            log.info("Step 4 ✓ SUCCESS: No existing accounts found");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_EXISTING_ACCOUNT", true, "No existing accounts found");

            // Step 5: Process AML
            log.info(">>> Step 5: PROCESS_AML");
            currentStep = AppConstants.PROCESS_AML;
            context.setAmlResult(complianceService.processAml(request));
            String amlStatus = context.getAmlResult() != null ? context.getAmlResult().getStatus().name() : "UNKNOWN";
            log.info("Step 5 ✓ SUCCESS: AML processing completed | Status: {}", amlStatus);
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "PROCESS_AML", true, "AML Status: " + amlStatus);
            complianceService.sentMessageOnHighRisk(request, context.getAmlResult());

            // Step 6: Create customer
            log.info(">>> Step 6: CREATE_CUSTOMER");
            currentStep = AppConstants.CREATE_CUSTOMER;
            CustomerCreationResult customerResult =
                    bankingService.createCustomerIfNeeded(request, context.getCustomerInfo());
            context.setCif(customerResult.getCif());
            context.setMnemonic(customerResult.getMnemonic());
            log.info("Step 6 ✓ SUCCESS: Customer created | CIF: {}, Mnemonic: {}", context.getCif(), context.getMnemonic());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_CUSTOMER", true, "CIF Created: " + customerResult.getCif());

            // Step 7: Create KHR Account
            log.info(">>> Step 7: CREATE_KHR_ACCOUNT");
            currentStep = AppConstants.CREATE_KHR_ACCOUNT;
            context.setKhrAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_KHR));
            log.info("Step 7 ✓ SUCCESS: KHR account created | Account: {}", context.getKhrAccount());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_KHR_ACCOUNT", true, "Account: " + context.getKhrAccount());

            // Step 8: Create USD Account
            log.info(">>> Step 8: CREATE_USD_ACCOUNT");
            currentStep = AppConstants.CREATE_USD_ACCOUNT;
            context.setUsdAccount(bankingService.createAccountIfNeeded(request, context.getCustomerInfo(),
                    context.getCif(), AppConstants.CURRENCY_USD));
            log.info("Step 8 ✓ SUCCESS: USD account created | Account: {}", context.getUsdAccount());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_USD_ACCOUNT", true, "Account: " + context.getUsdAccount());

            // Step 9: Final Validation
            log.info(">>> Step 9: VALIDATE_ACCOUNT_CREATION");
            currentStep = AppConstants.VALIDATE_ACCOUNT_CREATION;
            bankingService.validateAllRequiredAccountsCreated(context.getCif(),
                    context.getKhrAccount(),
                    context.getUsdAccount());
            log.info("Step 9 ✓ SUCCESS: All accounts validated");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_ACCOUNT_CREATION", true, "Accounts validated successfully");

            // Step 10: Activate mobile banking
            log.info(">>> Step 10: ACTIVATE_MOBILE_BANKING");
            currentStep = AppConstants.ACTIVATE_MOBILE_BANKING;
            context.setMbActivationCode(
                    bankingService.activateMobileBanking(request, context.getCif(),
                            context.getKhrAccount(), context.getUsdAccount()));
            log.info("Step 10 ✓ SUCCESS: Mobile banking activated");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "ACTIVATE_MOBILE_BANKING", true, "Mobile Banking activated");

            // BUILD RESPONSE
            CustomerResponse accInfo = complianceService.buildCustomerAccInfo(
                    context.getCif(), context.getKhrAccount(), context.getUsdAccount(),
                    context.getMnemonic());

            // PUBLISH SUCCESS EVENT
            eventPublisher.publishEvent(new AccountOpenedEvent(this, context));

            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("========== ACCOUNT OPENING COMPLETED ==========");
            log.info("✓ Successfully created accounts in {}ms", totalDuration);
            log.info("  • Legal ID: {}", request.getLegalId());
            log.info("  • CIF: {} | Mnemonic: {}", context.getCif(), context.getMnemonic());
            log.info("  • KHR Account: {}", context.getKhrAccount());
            log.info("  • USD Account: {}", context.getUsdAccount());
            log.info("  • Mobile Banking Code: {}", context.getMbActivationCode() != null ? "✓ Activated" : "N/A");

            return accInfo;

        } catch (Exception e) {
            log.error("========== ACCOUNT OPENING FAILED AT STEP: {} ==========", currentStep);

            // Monitor: Account opening failed
            monitoringService.logAccountOpeningFailed(
                    request.getLegalId(),
                    currentStep,
                    e.getMessage(),
                    e);

            // Save failure logs
            final String failureRemark = reportingService.buildFailureRemark(
                    currentStep,
                    context.getCif(),
                    context.getKhrAccount(),
                    context.getUsdAccount(),
                    context.getAmlResult());

            reportingService.saveFailureLogs(request, e, currentStep, failureRemark, false);

            throw e;
        }
    }

    @Override
    @Transactional
    public PendingAccountOpeningRequestDto submitAccountOpeningRequest(CustomerRequest request) throws Exception {
        log.info("========== ACCOUNT OPENING REQUEST SUBMISSION STARTED ==========");
        log.info("Legal ID: {}", request.getLegalId());

        // Check if legal_id already has PENDING request
        var existingPending = pendingRequestRepository.findByLegalIdAndStatus(
                request.getLegalId(), AccountOpeningRequestStatusEnum.PENDING);
        if (existingPending.isPresent()) {
            throw new BusinessException("Account opening request for legal ID " + request.getLegalId() +
                    " is already pending approval. Please wait for admin review.");
        }

        OpenAccountContext context = OpenAccountContext.builder().request(request).build();
        String currentStep = "START";
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Test connection
            log.info(">>> Step 1: TEST_CONNECTION");
            currentStep = AppConstants.TEST_CONNECTION;
            bankingService.testConnection();
            log.info("Step 1 ✓ SUCCESS: Database connection is healthy");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "TEST_CONNECTION", true,
                    "Banking service connection verified");

            // Step 2: Check existing complete account (recovery path for failed activation)
            log.info(">>> Step 2: CHECK_EXISTING_COMPLETE_ACCOUNT");
            currentStep = "CHECK_EXISTING_COMPLETE_ACCOUNT";
            var recoveryResult = bankingService.checkExistingCompleteAccountAndActivate(request);
            if (recoveryResult.isPresent()) {
                long totalDuration = System.currentTimeMillis() - startTime;
                log.info("Step 2 ✓ SUCCESS: Account recovery completed");
                log.info("========== ACCOUNT OPENING COMPLETED (RECOVERY) ==========");
                throw new BusinessException("Account already exists and is complete");
            }
            log.info("Step 2 ✓ INFO: No existing complete account found - continuing normal flow");

            // Step 3: Customer matching (continue normal flow)
            log.info(">>> Step 3: GET_CUSTOMER_INFO");
            currentStep = AppConstants.GET_CUSTOMER_INFO;
            context.setCustomerInfo(bankingService.getCustomerInfo(request.getLegalId()));
            String customerCif = context.getCustomerInfo() != null ? context.getCustomerInfo().get("CIF") : "N/A";
            log.info("Step 3 ✓ SUCCESS: Customer info retrieved");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "GET_CUSTOMER_INFO", true,
                    "Customer found with CIF: " + customerCif);

            // Step 4: Validation
            log.info(">>> Step 4: VALIDATE_EXISTING_ACCOUNTS");
            currentStep = AppConstants.VALIDATE_EXISTING_ACCOUNT;
            bankingService.validateExistingAccounts(context.getCustomerInfo());
            log.info("Step 4 ✓ SUCCESS: No existing accounts found");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_EXISTING_ACCOUNT",
                    true, "No existing accounts found");

            // Step 5: Process AML
            log.info(">>> Step 5: PROCESS_AML");
            currentStep = AppConstants.PROCESS_AML;
            context.setAmlResult(complianceService.processAml(request));
            String amlStatus = context.getAmlResult() != null ? context.getAmlResult().getStatus().name() : "UNKNOWN";
            log.info("Step 5 ✓ SUCCESS: AML processing completed | Status: {}", amlStatus);
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "PROCESS_AML", true,
                    "AML Status: " + amlStatus);
            complianceService.sentMessageOnHighRisk(request, context.getAmlResult());

            // Store pending request for admin review
            log.info(">>> STORING PENDING REQUEST FOR ADMIN REVIEW");
            PendingAccountOpeningRequest pendingRequest = PendingAccountOpeningRequest.builder()
                    .legalId(request.getLegalId())
                    .status(AccountOpeningRequestStatusEnum.PENDING)
                    .requestData(objectMapper.writeValueAsString(request))
                    .customerInfo(objectMapper.writeValueAsString(context.getCustomerInfo()))
                    .amlResultData(objectMapper.writeValueAsString(context.getAmlResult()))
                    .amlStatus(context.getAmlResult() != null ? context.getAmlResult().getStatus() : null)
                    .build();

            PendingAccountOpeningRequest saved = pendingRequestRepository.save(pendingRequest);
            log.info("Step 6 ✓ SUCCESS: Request stored for admin review | Request ID: {}", saved.getId());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "PENDING_ADMIN_REVIEW", true,
                    "Request stored with ID: " + saved.getId());

            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("========== ACCOUNT OPENING REQUEST SUBMISSION COMPLETED ==========");
            log.info("✓ Request submitted for admin review in {}ms", totalDuration);
            log.info("  • Legal ID: {}", request.getLegalId());
            log.info("  • Request ID: {}", saved.getId());
            log.info("  • AML Status: {}", amlStatus);

            return mapToDto(saved);

        } catch (BusinessException e) {
            log.error("========== BUSINESS EXCEPTION AT STEP: {} ==========", currentStep);
            monitoringService.logAccountOpeningFailed(request.getLegalId(), currentStep, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("========== ACCOUNT OPENING REQUEST SUBMISSION FAILED AT STEP: {} ==========", currentStep);
            monitoringService.logAccountOpeningFailed(request.getLegalId(), currentStep, e.getMessage(), e);
            final String failureRemark = reportingService.buildFailureRemark(currentStep, null, null, null,
                    context.getAmlResult());
            reportingService.saveFailureLogs(request, e, currentStep, failureRemark, false);
            throw e;
        }
    }

    @Override
    @Transactional
    public CustomerResponse completeAccountOpening(Long requestId) throws Exception {
        log.info("========== ACCOUNT OPENING COMPLETION STARTED ==========");
        log.info("Request ID: {}", requestId);

        PendingAccountOpeningRequest pendingRequest = pendingRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Account opening request not found with ID: " + requestId));

        if (pendingRequest.getStatus() != AccountOpeningRequestStatusEnum.APPROVED) {
            throw new BusinessException("Request must be in APPROVED status to complete account opening. Current status: " +
                    pendingRequest.getStatus());
        }

        try {
            // Deserialize stored data
            CustomerRequest request = objectMapper.readValue(pendingRequest.getRequestData(), CustomerRequest.class);
            @SuppressWarnings("unchecked")
            Map<String, String> customerInfo = objectMapper.readValue(pendingRequest.getCustomerInfo(), Map.class);

            OpenAccountContext context = OpenAccountContext.builder()
                    .request(request)
                    .customerInfo(customerInfo)
                    .build();

            String currentStep = "COMPLETION";
            long startTime = System.currentTimeMillis();

            // Step 0: Check if account already exists (staff might have opened in T24)
            log.info(">>> Step 0: CHECK_EXISTING_COMPLETE_ACCOUNT");
            currentStep = "CHECK_EXISTING_COMPLETE_ACCOUNT";
            var existingAccountResult = bankingService.checkExistingCompleteAccountAndActivate(request);
            if (existingAccountResult.isPresent()) {
                long totalDuration = System.currentTimeMillis() - startTime;
                log.info("Step 0 ✓ INFO: Account already exists in T24 (possibly opened by staff)");
                log.warn("========== ACCOUNT ALREADY EXISTS - RECOVERY MODE ==========");

                // Get existing account details
                var existingAccount = bankingService.getExistingAccountDetails(request.getLegalId());
                if (existingAccount.isPresent()) {
                    log.info("  • CIF: {} | Mnemonic: {}", existingAccount.get().getCif(), existingAccount.get().getMnemonic());
                    log.info("  • KHR Account: {}", existingAccount.get().getKhrAccount());
                    log.info("  • USD Account: {}", existingAccount.get().getUsdAccount());

                    // Update pending request status to COMPLETED since account already exists
                    pendingRequest.setStatus(AccountOpeningRequestStatusEnum.COMPLETED);
                    pendingRequestRepository.save(pendingRequest);
                    log.info("✓ Pending request status updated to COMPLETED (account already exists)");

                    monitoringService.logAccountOpeningStepProgress(request.getLegalId(),
                            "CHECK_EXISTING_COMPLETE_ACCOUNT", true,
                            "Account already exists with CIF: " + existingAccount.get().getCif());

                    return complianceService.buildCustomerAccInfo(
                            existingAccount.get().getCif(),
                            existingAccount.get().getKhrAccount(),
                            existingAccount.get().getUsdAccount(),
                            existingAccount.get().getMnemonic());
                }
            }
            log.info("Step 0 ✓ SUCCESS: No existing complete account found - continuing with creation");

            // Step 6: Create customer
            log.info(">>> Step 6: CREATE_CUSTOMER");
            currentStep = AppConstants.CREATE_CUSTOMER;
            CustomerCreationResult customerResult = bankingService.createCustomerIfNeeded(request, customerInfo);
            context.setCif(customerResult.getCif());
            context.setMnemonic(customerResult.getMnemonic());
            log.info("Step 6 ✓ SUCCESS: Customer created | CIF: {}, Mnemonic: {}", context.getCif(),
                    context.getMnemonic());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_CUSTOMER", true,
                    "CIF Created: " + customerResult.getCif());

            // Step 7: Create KHR Account
            log.info(">>> Step 7: CREATE_KHR_ACCOUNT");
            currentStep = AppConstants.CREATE_KHR_ACCOUNT;
            context.setKhrAccount(bankingService.createAccountIfNeeded(request, customerInfo,
                    context.getCif(), AppConstants.CURRENCY_KHR));
            log.info("Step 7 ✓ SUCCESS: KHR account created | Account: {}", context.getKhrAccount());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_KHR_ACCOUNT", true,
                    "Account: " + context.getKhrAccount());

            // Step 8: Create USD Account
            log.info(">>> Step 8: CREATE_USD_ACCOUNT");
            currentStep = AppConstants.CREATE_USD_ACCOUNT;
            context.setUsdAccount(bankingService.createAccountIfNeeded(request, customerInfo,
                    context.getCif(), AppConstants.CURRENCY_USD));
            log.info("Step 8 ✓ SUCCESS: USD account created | Account: {}", context.getUsdAccount());
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "CREATE_USD_ACCOUNT", true,
                    "Account: " + context.getUsdAccount());

            // Step 9: Final Validation
            log.info(">>> Step 9: VALIDATE_ACCOUNT_CREATION");
            currentStep = AppConstants.VALIDATE_ACCOUNT_CREATION;
            bankingService.validateAllRequiredAccountsCreated(context.getCif(),
                    context.getKhrAccount(),
                    context.getUsdAccount());
            log.info("Step 9 ✓ SUCCESS: All accounts validated");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "VALIDATE_ACCOUNT_CREATION",
                    true, "Accounts validated successfully");

            // Step 10: Activate mobile banking
            log.info(">>> Step 10: ACTIVATE_MOBILE_BANKING");
            currentStep = AppConstants.ACTIVATE_MOBILE_BANKING;
            context.setMbActivationCode(bankingService.activateMobileBanking(request, context.getCif(),
                    context.getKhrAccount(), context.getUsdAccount()));
            log.info("Step 10 ✓ SUCCESS: Mobile banking activated");
            monitoringService.logAccountOpeningStepProgress(request.getLegalId(), "ACTIVATE_MOBILE_BANKING", true,
                    "Mobile Banking activated");

            // BUILD RESPONSE
            CustomerResponse accInfo = complianceService.buildCustomerAccInfo(
                    context.getCif(), context.getKhrAccount(), context.getUsdAccount(),
                    context.getMnemonic());

            // Update pending request to COMPLETED
            pendingRequest.setStatus(AccountOpeningRequestStatusEnum.COMPLETED);
            pendingRequestRepository.save(pendingRequest);
            log.info("✓ Pending request status updated to COMPLETED");

            // PUBLISH SUCCESS EVENT
            eventPublisher.publishEvent(new AccountOpenedEvent(this, context));

            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("========== ACCOUNT OPENING COMPLETED ==========");
            log.info("✓ Successfully created accounts in {}ms", totalDuration);
            log.info("  • Legal ID: {}", request.getLegalId());
            log.info("  • CIF: {} | Mnemonic: {}", context.getCif(), context.getMnemonic());
            log.info("  • KHR Account: {}", context.getKhrAccount());
            log.info("  • USD Account: {}", context.getUsdAccount());
            log.info("  • Mobile Banking Code: {}", context.getMbActivationCode() != null ? "✓ Activated" : "N/A");

            return accInfo;

        } catch (Exception e) {
            log.error("========== ACCOUNT OPENING COMPLETION FAILED ==========");
            monitoringService.logAccountOpeningFailed(pendingRequest.getLegalId(), currentStep, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public PendingAccountOpeningRequestDto approveAccountOpeningRequest(ApproveAccountOpeningRequestDto dto)
            throws Exception {
        log.info("========== APPROVING ACCOUNT OPENING REQUEST ==========");
        log.info("Request ID: {}", dto.getRequestId());

        PendingAccountOpeningRequest pendingRequest = pendingRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new NotFoundException("Request not found with ID: " + dto.getRequestId()));

        if (pendingRequest.getStatus() != AccountOpeningRequestStatusEnum.PENDING) {
            throw new BusinessException("Only PENDING requests can be approved. Current status: " +
                    pendingRequest.getStatus());
        }

        pendingRequest.setStatus(AccountOpeningRequestStatusEnum.APPROVED);
        pendingRequest.setApprovedBy(securityUtils.getCurrentUsername());
        pendingRequest.setApprovedAt(System.currentTimeMillis());
        pendingRequest.setApprovalRemark(dto.getApprovalRemark());

        PendingAccountOpeningRequest saved = pendingRequestRepository.save(pendingRequest);
        log.info("✓ Request approved | Legal ID: {}", saved.getLegalId());

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public PendingAccountOpeningRequestDto rejectAccountOpeningRequest(RejectAccountOpeningRequestDto dto)
            throws Exception {
        log.info("========== REJECTING ACCOUNT OPENING REQUEST ==========");
        log.info("Request ID: {}", dto.getRequestId());

        PendingAccountOpeningRequest pendingRequest = pendingRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new NotFoundException("Request not found with ID: " + dto.getRequestId()));

        if (pendingRequest.getStatus() != AccountOpeningRequestStatusEnum.PENDING) {
            throw new BusinessException("Only PENDING requests can be rejected. Current status: " +
                    pendingRequest.getStatus());
        }

        pendingRequest.setStatus(AccountOpeningRequestStatusEnum.REJECTED);
        pendingRequest.setRejectedBy(securityUtils.getCurrentUsername());
        pendingRequest.setRejectedAt(System.currentTimeMillis());
        pendingRequest.setRejectionReason(dto.getRejectionReason());

        PendingAccountOpeningRequest saved = pendingRequestRepository.save(pendingRequest);
        log.info("✓ Request rejected | Legal ID: {}", saved.getLegalId());

        return mapToDto(saved);
    }

    @Override
    public List<PendingAccountOpeningRequestDto> getPendingRequests() {
        return pendingRequestRepository.findByStatus(AccountOpeningRequestStatusEnum.PENDING)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PendingAccountOpeningRequestDto getPendingRequest(Long requestId) throws Exception {
        PendingAccountOpeningRequest pendingRequest = pendingRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found with ID: " + requestId));
        return mapToDto(pendingRequest);
    }

    private PendingAccountOpeningRequestDto mapToDto(PendingAccountOpeningRequest entity) {
        return PendingAccountOpeningRequestDto.builder()
                .id(entity.getId())
                .legalId(entity.getLegalId())
                .status(entity.getStatus())
                .amlStatus(entity.getAmlStatus())
                .rejectionReason(entity.getRejectionReason())
                .rejectedBy(entity.getRejectedBy())
                .approvalRemark(entity.getApprovalRemark())
                .approvedBy(entity.getApprovedBy())
                .createdAt(entity.getCreatedAt())
                .approvedAt(entity.getApprovedAt())
                .rejectedAt(entity.getRejectedAt())
                .build();
    }
}