package com.internal.feature.aml.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.aml.dto.request.AllAmlHistoryRequestDto;
import com.internal.feature.aml.dto.request.AllAmlRequestDto;
import com.internal.feature.aml.dto.request.ExternalAmlStatusUpdateDto;
import com.internal.feature.aml.dto.request.UpdateAmlStatusDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlHistoryDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.service.AmlService;
import com.internal.feature.auth.service.ApiKeyService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/aml")
@RequiredArgsConstructor
@Slf4j
public class AmlController {

    private final AmlService service;

    @PostMapping("/all-history")
    public ResponseEntity<ApiResponse<AllAmlHistoryResponseDto>> getAllHistory(@RequestBody AllAmlHistoryRequestDto request) {
        log.info("Fetching all AML history");
        AllAmlHistoryResponseDto list = service.getAllAmlHistory(request);
        log.info("Successfully retrieved {} AML history records", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.AML_HISTORY_RETRIEVED, list));
    }

    @PostMapping("/all-status")
    public ResponseEntity<ApiResponse<AllAmlResponseDto>> getAllStatus(@RequestBody AllAmlRequestDto request) {
        log.info("Fetching all AML statuses with search: {}", request.getSearch());
        AllAmlResponseDto list = service.getAllAml(request);
        log.info("Successfully retrieved {} AML status records", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.AML_STATUSES_RETRIEVED, list));
    }

    @PostMapping("/status-by-id/{id}")
    public ResponseEntity<ApiResponse<AmlStatusDto>> getAmlById(@PathVariable Long id) {
        log.info("Fetching AML statuses by id: : {}", id);
        AmlStatusDto amlStatusDto = service.getAmlById(id);
        log.info("Successfully retrieved {} AML status records", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.AML_STATUS_RETRIEVED, amlStatusDto));
    }

    @PostMapping("/history-by-id/{id}")
    public ResponseEntity<ApiResponse<AmlHistoryDto>> getAmlHistoryById(@PathVariable Long id) {
        log.info("Fetching AML statuses by id: : {}", id);
        AmlHistoryDto amlStatusDto = service.getAmlHistoryById(id);
        log.info("Successfully retrieved {} AML status records", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.AML_STATUS_RETRIEVED, amlStatusDto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<AmlStatusDto>> updateAmlStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAmlStatusDto req
            ) throws JsonProcessingException {
        log.info("Updating AML status for ID: {} to {}", id, req.getStatus());
        AmlStatusDto updatedStatus = service.updateAmlStatus(id, req);
        log.info("AML status updated successfully: {}", updatedStatus.getId());
        return ResponseEntity.ok(ApiResponse.success(
                ResponseMessage.AML_STATUS_UPDATED, updatedStatus
        ));
    }


    private final ApiKeyService apiKeyService;

    @PostMapping("/external/update-status")
    public ResponseEntity<ApiResponse<String>> updateExternalAmlStatus(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestHeader("X-SECRET-KEY") String secretKey,
            @RequestBody @Valid ExternalAmlStatusUpdateDto req) {

        if (!apiKeyService.validateKey(apiKey, secretKey)) {
            log.warn("Authentication failed for external AML update");
            return ResponseEntity.status(401).body(ApiResponse.error(ResponseMessage.INVALID_API_KEY));
        }

        log.info("Received external request to update AML status for Legal ID: {}", req.getCustomerId());
        service.updateExternalAmlStatus(req);
        
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.AML_EXTERNAL_UPDATED, null));
    }
}
