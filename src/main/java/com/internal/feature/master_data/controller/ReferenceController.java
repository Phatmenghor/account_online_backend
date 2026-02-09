package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.GetAllReferenceRequest;
import com.internal.feature.master_data.dto.request.ReferenceCreateRequestDto;
import com.internal.feature.master_data.dto.request.ReferenceUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllReferenceResponseDto;
import com.internal.feature.master_data.dto.response.ReferenceDto;
import com.internal.feature.master_data.service.ReferenceService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reference/banks")
@RequiredArgsConstructor
@Slf4j
public class ReferenceController {

    private final ReferenceService service;

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<ReferenceDto>> getById(@PathVariable Long id) {
        log.info("Fetching bank with ID: {}", id);
        ReferenceDto dto = service.getById(id);
        log.info("Successfully retrieved bank with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.BANK), dto));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllReferenceResponseDto>> getAll(@RequestBody GetAllReferenceRequest request) {
        log.info("Fetching all banks");
        AllReferenceResponseDto list = service.getAll(request);
        log.info("Successfully retrieved {} banks", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_BANKS), list));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ReferenceDto>> create(@RequestBody ReferenceCreateRequestDto request) {
        log.info("Creating new bank: {}", request);
        ReferenceDto dto = service.create(request);
        log.info("Successfully created bank with ID: {}", dto.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.created(ResponseMessage.BANK), dto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ReferenceDto>> update(@PathVariable Long id,
                                                            @RequestBody ReferenceUpdateRequestDto request) {
        log.info("Updating bank with ID: {} with data: {}", id, request);
        ReferenceDto dto = service.update(id, request);
        log.info("Successfully updated bank with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.updated(ResponseMessage.BANK), dto));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<ReferenceDto>> delete(@PathVariable Long id) {
        log.info("Deleting bank with ID: {}", id);
        ReferenceDto referenceDto = service.delete(id);
        log.info("Successfully deleted bank with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.deleted(ResponseMessage.BANK), referenceDto));
    }
}
