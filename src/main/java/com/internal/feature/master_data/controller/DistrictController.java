package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.DistrictRequestDto;
import com.internal.feature.master_data.dto.response.DistrictResponseDto;
import com.internal.feature.master_data.service.DistrictService;
import com.internal.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/master-data/address")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "District Management")
public class DistrictController {

    private final DistrictService districtService;

    @PostMapping("/districts/all")
    public ResponseEntity<ApiResponse<PaginationResponse<DistrictResponseDto>>> getAllDistricts(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("All districts retrieved successfully", districtService.getAllDistricts(request)));
    }

    @PostMapping("/districts/get-by-id/{id}")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> getDistrictById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("District retrieved successfully", districtService.getDistrictById(id)));
    }

    @PostMapping("/districts/create")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> createDistrict(@RequestBody DistrictRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("District created successfully", districtService.createDistrict(request)));
    }

    @PostMapping("/districts/update/{id}")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> updateDistrict(@PathVariable Long id, @RequestBody DistrictRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("District updated successfully", districtService.updateDistrict(id, request)));
    }

    @PostMapping("/districts/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDistrict(@PathVariable Long id) {
        districtService.deleteDistrict(id);
        return ResponseEntity.ok(ApiResponse.success("District deleted successfully", null));
    }
}
