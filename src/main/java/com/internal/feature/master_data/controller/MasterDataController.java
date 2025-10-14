package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.service.masterService.MasterDataService;
import com.internal.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/master-data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Master Data Management")
public class MasterDataController {

    private final MasterDataService masterDataService;

    @PostMapping("/province")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsProvinceDto>>> getProvinces(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        PaginationResponse<ClsProvinceDto> response = masterDataService.getProvince(pageNo, pageSize);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Provinces retrieved successfully!", response)
        );
    }

    @PostMapping("/district/{provinceCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsDistrictDto>>> getDistrict(
            @PathVariable String provinceCode,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        PaginationResponse<ClsDistrictDto> response = masterDataService.getDistrict(provinceCode, pageNo, pageSize);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Districts retrieved successfully!", response)
        );
    }

    @PostMapping("/commune/{districtCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsCommuneDto>>> getCommunes(
            @PathVariable String districtCode,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        PaginationResponse<ClsCommuneDto> response = masterDataService.getCommune(districtCode, pageNo, pageSize);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Communes retrieved successfully!", response)
        );
    }

    @PostMapping("/village/{communeCode}")
    public ResponseEntity<ApiResponse<PaginationResponse<ClsVillageDto>>> getVillages(
            @PathVariable String communeCode,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        PaginationResponse<ClsVillageDto> response = masterDataService.getVillage(communeCode, pageNo, pageSize);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Villages retrieved successfully!", response)
        );
    }
}
