package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.ProvinceRequestDto;
import com.internal.feature.master_data.dto.response.ProvinceResponseDto;
import com.internal.feature.master_data.service.ProvinceService;
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
@Tag(name = "Province Management")
public class ProvinceController {

    private final ProvinceService provinceService;

    @PostMapping("/provinces/all")
    public ResponseEntity<ApiResponse<PaginationResponse<ProvinceResponseDto>>> getAllProvinces(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("All provinces retrieved successfully", provinceService.getAllProvinces(request)));
    }

    @PostMapping("/provinces/get-by-id/{id}")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> getProvinceById( @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Province retrieved successfully", provinceService.getProvinceById(id)));
    }

    @PostMapping("/provinces/create")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> createProvince(@RequestBody ProvinceRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Province created successfully", provinceService.createProvince(request)));
    }

    @PostMapping("/provinces/update/{id}")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> updateProvince(@PathVariable Long id, @RequestBody ProvinceRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Province updated successfully", provinceService.updateProvince(id, request)));
    }

    @PostMapping("/provinces/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProvince(@PathVariable Long id) {
        provinceService.deleteProvince(id);
        return ResponseEntity.ok(ApiResponse.success("Province deleted successfully", null));
    }
}
