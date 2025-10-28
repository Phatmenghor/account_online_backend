package com.internal.feature.aml.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.aml.dto.request.AllAmlHistoryRequestDto;
import com.internal.feature.aml.dto.request.AllAmlRequestDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.service.AmlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/aml")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Aml Management")
public class AmlController {

    private final AmlService service;

    /** Get all AML history */
    @PostMapping("/all-history")
    public ResponseEntity<ApiResponse<AllAmlHistoryResponseDto>> getAllHistory(@RequestBody AllAmlHistoryRequestDto request) {
        log.info("Fetching all AML history");
        AllAmlHistoryResponseDto list = service.getAllAmlHistory(request);
        log.info("Successfully retrieved {} AML history records", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("All AML history retrieved successfully", list));
    }

    /** Get all AML statuses */
    @PostMapping("/all-status")
    public ResponseEntity<ApiResponse<AllAmlResponseDto>> getAllStatus(@RequestBody AllAmlRequestDto request) {
        log.info("Fetching all AML statuses with search: {}", request.getSearch());
        AllAmlResponseDto list = service.getAllAml(request);
        log.info("Successfully retrieved {} AML status records", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("All AML statuses retrieved successfully", list));
    }

    /** Approve AML status */
    @PostMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<AmlStatusDto>> approveAml(@PathVariable Long id) {
        log.info("Approving AML status with ID: {}", id);
        AmlStatusDto approvedStatus = service.approveAmlStatus(id);
        log.info("AML status approved: {}", approvedStatus.getId());
        return ResponseEntity.ok(ApiResponse.success("AML status approved successfully", approvedStatus));
    }

    /** Reject AML status */
    @PostMapping("/reject/{id}")
    public ResponseEntity<ApiResponse<AmlStatusDto>> rejectAml(@PathVariable Long id) {
        log.info("Rejecting AML status with ID: {}", id);
        AmlStatusDto rejectedStatus = service.rejectAmlStatus(id);
        log.info("AML status rejected: {}", rejectedStatus.getId());
        return ResponseEntity.ok(ApiResponse.success("AML status rejected successfully", rejectedStatus));
    }
}
