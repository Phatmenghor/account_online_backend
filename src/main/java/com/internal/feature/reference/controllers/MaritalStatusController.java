package com.internal.feature.reference.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.reference.dto.request.GetAllMaritalStatusRequest;
import com.internal.feature.reference.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.reference.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.reference.dto.response.MaritalStatusDto;
import com.internal.feature.reference.service.MaritalStatusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reference/marital-status")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Marital Status Management")
public class MaritalStatusController {

    private final MaritalStatusService service;

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<MaritalStatusDto>> getById(@PathVariable Long id) {
        log.info("Fetching marital status with ID: {}", id);
        MaritalStatusDto dto = service.getById(id);
        log.info("Successfully retrieved marital status with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Marital status retrieved successfully", dto));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<List<MaritalStatusDto>>> getAll(@RequestBody GetAllMaritalStatusRequest request) {
        log.info("Fetching all marital statuses");
        List<MaritalStatusDto> list = service.getAll(request);
        log.info("Successfully retrieved {} marital statuses", list.size());
        return ResponseEntity.ok(ApiResponse.success("All marital statuses retrieved successfully", list));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MaritalStatusDto>> create(@RequestBody MaritalStatusCreateRequestDto request) {
        log.info("Creating new marital status: {}", request);
        MaritalStatusDto dto = service.create(request);
        log.info("Successfully created marital status with ID: {}", dto.getId());
        return ResponseEntity.ok(ApiResponse.success("Marital status created successfully", dto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MaritalStatusDto>> update(@PathVariable Long id,
                                                                @RequestBody MaritalStatusUpdateRequestDto request) {
        log.info("Updating marital status with ID: {} with data: {}", id, request);
        MaritalStatusDto dto = service.update(id, request);
        log.info("Successfully updated marital status with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Marital status updated successfully", dto));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting marital status with ID: {}", id);
        service.delete(id);
        log.info("Successfully deleted marital status with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Marital status deleted successfully", null));
    }
}
