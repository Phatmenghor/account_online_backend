package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AddressRequestDto;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.utils.constants.ResponseMessage;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/master-data")
@RequiredArgsConstructor
@Slf4j
public class MasterDataController {

    private final MasterDataService masterDataService;

    @PostMapping("/init/address")
    public ResponseEntity<ApiResponse<LocationCodesDto>> initAddress(
            @RequestBody AddressRequestDto address) {

        LocationCodesDto response = masterDataService.initAddress(address);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ADDRESS_INIT, response));
    }

    @PostMapping("/init/place-of-birth")
    public ResponseEntity<ApiResponse<LocationCodesDto>> initPob(
            @RequestBody AddressRequestDto address) {

        LocationCodesDto response = masterDataService.initPob(address);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.POB_INIT, response));
    }

    @PostMapping("/province")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsProvinceDto>>> getProvinces(
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsProvinceDto> response = masterDataService.getProvince(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROVINCES_RETRIEVED, response));
    }

    @PostMapping("/district/{provinceCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsDistrictDto>>> getDistricts(
            @PathVariable String provinceCode,
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsDistrictDto> response = masterDataService.getDistrict(request, provinceCode);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.DISTRICTS_RETRIEVED, response));
    }

    @PostMapping("/commune/{districtCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsCommuneDto>>> getCommunes(
            @PathVariable String districtCode,
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsCommuneDto> response = masterDataService.getCommune(request, districtCode);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.COMMUNES_RETRIEVED, response));
    }

    @PostMapping("/village/{communeCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsVillageDto>>> getVillages(
            @PathVariable String communeCode,
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsVillageDto> response = masterDataService.getVillage(request, communeCode);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.VILLAGES_RETRIEVED, response));
    }

    @PostMapping("/branch")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsBranchDto>>> getBranches(
            @RequestBody AllMasterDataRequest request) {

        PaginationResponse<ClsBranchDto> response = masterDataService.getBranch(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.BRANCHES_RETRIEVED, response));
    }
}
