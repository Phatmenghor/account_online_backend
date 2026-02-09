package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.DistrictRequestDto;
import com.internal.feature.master_data.dto.response.DistrictResponseDto;
import com.internal.feature.master_data.service.DistrictService;
import com.internal.utils.constants.ResponseMessage;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/master-data/address")
@RequiredArgsConstructor
@Slf4j
public class DistrictController {

    private final DistrictService districtService;

    @PostMapping("/districts/all")
    public ResponseEntity<ApiResponse<PaginationResponse<DistrictResponseDto>>> getAllDistricts(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_DISTRICTS), districtService.getAllDistricts(request)));
    }

    @PostMapping("/districts/get-by-id/{id}")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> getDistrictById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.DISTRICT), districtService.getDistrictById(id)));
    }

    @PostMapping("/districts/create")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> createDistrict(@RequestBody DistrictRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.created(ResponseMessage.DISTRICT), districtService.createDistrict(request)));
    }

    @PostMapping("/districts/update/{id}")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> updateDistrict(@PathVariable Long id, @RequestBody DistrictRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.updated(ResponseMessage.DISTRICT), districtService.updateDistrict(id, request)));
    }

    @PostMapping("/districts/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDistrict(@PathVariable Long id) {
        districtService.deleteDistrict(id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.deleted(ResponseMessage.DISTRICT), null));
    }
}
