package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AddressRequestDto;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/master-data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Master Data Management (public)")
public class MasterDataController {

    private final MasterDataService masterDataService;

    @PostMapping("/init-address")
    public ResponseEntity<ApiResponse<LocationCodesDto>> initAddress(
            @RequestBody AddressRequestDto address) {

        LocationCodesDto response = masterDataService.initAddress(address);
        return ResponseEntity.ok(ApiResponse.success("Address init successfully!", response));
    }

    @PostMapping("/init-pob")
    public ResponseEntity<ApiResponse<LocationCodesDto>> initPob(
            @RequestBody AddressRequestDto address) {

        LocationCodesDto response = masterDataService.initPob(address);
        return ResponseEntity.ok(ApiResponse.success("Pob init successfully!", response));
    }

    @PostMapping("/province")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsProvinceDto>>> getProvinces(
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsProvinceDto> response = masterDataService.getProvince(request);
        return ResponseEntity.ok(ApiResponse.success("Provinces retrieved successfully!", response));
    }

    @PostMapping("/district/{provinceCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsDistrictDto>>> getDistricts(
            @PathVariable String provinceCode,
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsDistrictDto> response = masterDataService.getDistrict(request, provinceCode);
        return ResponseEntity.ok(ApiResponse.success("Districts retrieved successfully!", response));
    }

    @PostMapping("/commune/{districtCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsCommuneDto>>> getCommunes(
            @PathVariable String districtCode,
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsCommuneDto> response = masterDataService.getCommune(request, districtCode);
        return ResponseEntity.ok(ApiResponse.success("Communes retrieved successfully!", response));
    }

    @PostMapping("/village/{communeCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsVillageDto>>> getVillages(
            @PathVariable String communeCode,
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsVillageDto> response = masterDataService.getVillage(request, communeCode);
        return ResponseEntity.ok(ApiResponse.success("Villages retrieved successfully!", response));
    }

    @PostMapping("/branch")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsBranchDto>>> getBranches(
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsBranchDto> response = masterDataService.getBranch(request);
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved successfully!", response));
    }
}
