package com.internal.feature.reference.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.reference.dto.request.GetAllReferenceBankRequest;
import com.internal.feature.reference.dto.request.ReferenceBankCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceBankUpdateRequestDto;
import com.internal.feature.reference.dto.response.ReferenceBankDto;
import com.internal.feature.reference.service.ReferenceBankService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reference/banks")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Bank Reference Management")
public class ReferenceBankController {

    private final ReferenceBankService service;

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<ReferenceBankDto>> getById(@PathVariable Long id) {
        log.info("Fetching bank with ID: {}", id);
        ReferenceBankDto dto = service.getById(id);
        log.info("Successfully retrieved bank with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Bank retrieved successfully", dto));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<List<ReferenceBankDto>>> getAll(@RequestBody GetAllReferenceBankRequest request) {
        log.info("Fetching all banks");
        List<ReferenceBankDto> list = service.getAll(request);
        log.info("Successfully retrieved {} banks", list.size());
        return ResponseEntity.ok(ApiResponse.success("All banks retrieved successfully", list));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ReferenceBankDto>> create(@RequestBody ReferenceBankCreateRequestDto request) {
        log.info("Creating new bank: {}", request);
        ReferenceBankDto dto = service.create(request);
        log.info("Successfully created bank with ID: {}", dto.getId());
        return ResponseEntity.ok(ApiResponse.success("Bank created successfully", dto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ReferenceBankDto>> update(@PathVariable Long id,
                                                                @RequestBody ReferenceBankUpdateRequestDto request) {
        log.info("Updating bank with ID: {} with data: {}", id, request);
        ReferenceBankDto dto = service.update(id, request);
        log.info("Successfully updated bank with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Bank updated successfully", dto));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting bank with ID: {}", id);
        service.delete(id);
        log.info("Successfully deleted bank with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Bank deleted successfully", null));
    }
}
