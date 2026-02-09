package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.*;
import com.internal.feature.master_data.dto.response.AllLegalTypeResponseDto;
import com.internal.feature.master_data.dto.response.LegalTypeDto;
import com.internal.feature.master_data.service.LegalTypeService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/legal-type")
@RequiredArgsConstructor
@Slf4j
public class LegalTypeController {
    private final LegalTypeService service;

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<LegalTypeDto>> getById(@PathVariable Long id) {
        log.info("Fetching legal type with ID: {}", id);
        LegalTypeDto dto = service.getById(id);
        log.info("Successfully retrieved legal type with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.LEGAL_TYPE), dto));
    }
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllLegalTypeResponseDto>> getAll(@RequestBody GetAllLegalTypeRequest request) {
        log.info("Fetching all legal types");
        AllLegalTypeResponseDto list = service.getAllLegalType(request);
        log.info("Successfully retrieved {} legal types", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_LEGAL_TYPES), list));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<LegalTypeDto>> create(@RequestBody LegalTypeCreateRequestDto request) {
        log.info("Creating new legal type: {}", request);
        LegalTypeDto dto = service.create(request);
        log.info("Successfully created legal type with ID: {}", dto.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.created(ResponseMessage.LEGAL_TYPE), dto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<LegalTypeDto>> update(@PathVariable Long id,
                                                                @RequestBody LegalTypeUpdateRequestDto request) {
        log.info("Updating legal type with ID: {} with data: {}", id, request);
        LegalTypeDto dto = service.update(id, request);
        log.info("Successfully updated legal type with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.updated(ResponseMessage.LEGAL_TYPE), dto));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<LegalTypeDto>> delete(@PathVariable Long id) {
        log.info("Deleting legal type with ID: {}", id);
        LegalTypeDto legalTypeDto = service.delete(id);
        log.info("Successfully deleted legal type with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.deleted(ResponseMessage.LEGAL_TYPE), legalTypeDto));
    }

}
