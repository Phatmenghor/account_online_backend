package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.GetAllMaritalStatusRequest;
import com.internal.feature.master_data.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.master_data.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllMaritalStatusResponseDto;
import com.internal.feature.master_data.dto.response.MaritalStatusDto;
import com.internal.feature.master_data.service.MaritalStatusService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/marital-status")
@RequiredArgsConstructor
@Slf4j
public class MaritalStatusController {

    private final MaritalStatusService service;

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<MaritalStatusDto>> getById(@PathVariable Long id) {
        log.info("Fetching marital status with ID: {}", id);
        MaritalStatusDto dto = service.getById(id);
        log.info("Successfully retrieved marital status with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.MARITAL_STATUS), dto));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllMaritalStatusResponseDto>> getAll(@RequestBody GetAllMaritalStatusRequest request) {
        log.info("Fetching all marital statuses");
        AllMaritalStatusResponseDto list = service.getAll(request);
        log.info("Successfully retrieved {} marital statuses", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_MARITAL_STATUSES), list));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MaritalStatusDto>> create(@RequestBody MaritalStatusCreateRequestDto request) {
        log.info("Creating new marital status: {}", request);
        MaritalStatusDto dto = service.create(request);
        log.info("Successfully created marital status with ID: {}", dto.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.created(ResponseMessage.MARITAL_STATUS), dto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MaritalStatusDto>> update(@PathVariable Long id,
                                                                @RequestBody MaritalStatusUpdateRequestDto request) {
        log.info("Updating marital status with ID: {} with data: {}", id, request);
        MaritalStatusDto dto = service.update(id, request);
        log.info("Successfully updated marital status with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.updated(ResponseMessage.MARITAL_STATUS), dto));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<MaritalStatusDto>> delete(@PathVariable Long id) {
        log.info("Deleting marital status with ID: {}", id);
        MaritalStatusDto maritalStatusDto = service.delete(id);
        log.info("Successfully deleted marital status with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.deleted(ResponseMessage.MARITAL_STATUS), maritalStatusDto));
    }
}
