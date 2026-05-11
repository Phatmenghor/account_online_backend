package com.internal.feature.open_account.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.utils.constants.AppConstants;
import com.internal.feature.open_account.dto.request.ApproveAccountOpeningRequestDto;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.request.RejectAccountOpeningRequestDto;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.dto.response.PendingAccountOpeningRequestDto;
import com.internal.feature.open_account.service.OpenAccountService;
import com.internal.feature.open_account.service.external.T24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/open-account")
@RequiredArgsConstructor
@Slf4j
public class OpenAccountController {
    private final T24Service t24Service;
    private final OpenAccountService openAccountService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> openAccount(@Valid @RequestBody CustomerRequest request) throws Exception {
        log.info("Received account opening request for Legal ID: {}", request.getLegalId());

        CustomerResponse response = openAccountService.openAccount(request);

        log.info("========== ACCOUNT OPENING COMPLETED ==========");
        log.info("✓ CIF: {} | Mnemonic: {} | KHR: {} | USD: {}",
                response.getCif(), response.getMnemonic(), response.getKhrAccount(), response.getUsdAccount());

        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_SUCCESS, response));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<PendingAccountOpeningRequestDto>> submitAccountOpeningRequest(
            @Valid @RequestBody CustomerRequest request) throws Exception {
        log.info("Received account opening submission for Legal ID: {}", request.getLegalId());

        PendingAccountOpeningRequestDto response = openAccountService.submitAccountOpeningRequest(request);

        log.info("✓ Account opening request submitted for admin review | Request ID: {}", response.getId());
        log.info("  Legal ID: {} | AML Status: {}", response.getLegalId(), response.getAmlStatus());

        return ResponseEntity.ok(ApiResponse.success("Account opening request submitted for admin review", response));
    }

    @PostMapping("/complete/{requestId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> completeAccountOpening(@PathVariable Long requestId) throws Exception {
        log.info("Completing account opening for Request ID: {}", requestId);

        CustomerResponse response = openAccountService.completeAccountOpening(requestId);

        log.info("========== ACCOUNT OPENING COMPLETED ==========");
        log.info("✓ CIF: {} | Mnemonic: {} | KHR: {} | USD: {}",
                response.getCif(), response.getMnemonic(), response.getKhrAccount(), response.getUsdAccount());

        return ResponseEntity.ok(ApiResponse.success("Account opening completed successfully", response));
    }

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<PendingAccountOpeningRequestDto>> approveRequest(
            @Valid @RequestBody ApproveAccountOpeningRequestDto dto) throws Exception {
        log.info("Approving account opening request ID: {}", dto.getRequestId());

        PendingAccountOpeningRequestDto response = openAccountService.approveAccountOpeningRequest(dto);

        log.info("✓ Request approved | Legal ID: {}", response.getLegalId());

        return ResponseEntity.ok(ApiResponse.success("Request approved successfully", response));
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<PendingAccountOpeningRequestDto>> rejectRequest(
            @Valid @RequestBody RejectAccountOpeningRequestDto dto) throws Exception {
        log.info("Rejecting account opening request ID: {}", dto.getRequestId());

        PendingAccountOpeningRequestDto response = openAccountService.rejectAccountOpeningRequest(dto);

        log.info("✓ Request rejected | Legal ID: {}", response.getLegalId());

        return ResponseEntity.ok(ApiResponse.success("Request rejected successfully", response));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingAccountOpeningRequestDto>>> getPendingRequests() {
        log.info("Fetching all pending account opening requests");

        List<PendingAccountOpeningRequestDto> response = openAccountService.getPendingRequests();

        log.info("✓ Found {} pending requests", response.size());

        return ResponseEntity.ok(ApiResponse.success("Pending requests retrieved successfully", response));
    }

    @GetMapping("/pending/{requestId}")
    public ResponseEntity<ApiResponse<PendingAccountOpeningRequestDto>> getPendingRequest(@PathVariable Long requestId) throws Exception {
        log.info("Fetching pending request ID: {}", requestId);

        PendingAccountOpeningRequestDto response = openAccountService.getPendingRequest(requestId);

        return ResponseEntity.ok(ApiResponse.success("Request retrieved successfully", response));
    }
}
