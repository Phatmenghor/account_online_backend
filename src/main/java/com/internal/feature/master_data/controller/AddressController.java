package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.CommuneRequestDto;
import com.internal.feature.master_data.dto.request.DistrictRequestDto;
import com.internal.feature.master_data.dto.request.ProvinceRequestDto;
import com.internal.feature.master_data.dto.request.VillageRequestDto;
import com.internal.feature.master_data.dto.response.CommuneResponseDto;
import com.internal.feature.master_data.dto.response.DistrictResponseDto;
import com.internal.feature.master_data.dto.response.ProvinceResponseDto;
import com.internal.feature.master_data.dto.response.VillageResponseDto;
import com.internal.feature.master_data.service.AddressService;
import com.internal.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data/address")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Address Management")
public class AddressController {

    private final AddressService addressService;

    // Province
    @PostMapping("/provinces/all")
    public ResponseEntity<ApiResponse<PaginationResponse<ProvinceResponseDto>>> getAllProvinces(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("All provinces retrieved successfully", addressService.getAllProvinces(request)));
    }

    @PostMapping("/provinces/get-by-code/{code}")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> getProvinceByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("Province retrieved successfully", addressService.getProvinceByCode(code)));
    }

    @PostMapping("/provinces/create")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> createProvince(@RequestBody ProvinceRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Province created successfully", addressService.createProvince(request)));
    }

    @PostMapping("/provinces/update/{code}")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> updateProvince(@PathVariable String code, @RequestBody ProvinceRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Province updated successfully", addressService.updateProvince(code, request)));
    }

    @PostMapping("/provinces/delete/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteProvince(@PathVariable String code) {
        addressService.deleteProvince(code);
        return ResponseEntity.ok(ApiResponse.success("Province deleted successfully", null));
    }

    // District
    @PostMapping("/districts/get-by-province")
    public ResponseEntity<ApiResponse<PaginationResponse<DistrictResponseDto>>> getDistrictsByProvince(@RequestParam String provinceCode, @RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Districts retrieved successfully", addressService.getDistrictsByProvince(request, provinceCode)));
    }

    @PostMapping("/districts/get-by-code/{code}")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> getDistrictByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("District retrieved successfully", addressService.getDistrictByCode(code)));
    }

    @PostMapping("/districts/create")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> createDistrict(@RequestBody DistrictRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("District created successfully", addressService.createDistrict(request)));
    }

    @PostMapping("/districts/update/{code}")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> updateDistrict(@PathVariable String code, @RequestBody DistrictRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("District updated successfully", addressService.updateDistrict(code, request)));
    }

    @PostMapping("/districts/delete/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteDistrict(@PathVariable String code) {
        addressService.deleteDistrict(code);
        return ResponseEntity.ok(ApiResponse.success("District deleted successfully", null));
    }

    // Commune
    @PostMapping("/communes/get-by-district")
    public ResponseEntity<ApiResponse<PaginationResponse<CommuneResponseDto>>> getCommunesByDistrict(@RequestParam String districtCode, @RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Communes retrieved successfully", addressService.getCommunesByDistrict(request, districtCode)));
    }

    @PostMapping("/communes/get-by-code/{code}")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> getCommuneByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("Commune retrieved successfully", addressService.getCommuneByCode(code)));
    }

    @PostMapping("/communes/create")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> createCommune(@RequestBody CommuneRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Commune created successfully", addressService.createCommune(request)));
    }

    @PostMapping("/communes/update/{code}")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> updateCommune(@PathVariable String code, @RequestBody CommuneRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Commune updated successfully", addressService.updateCommune(code, request)));
    }

    @PostMapping("/communes/delete/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteCommune(@PathVariable String code) {
        addressService.deleteCommune(code);
        return ResponseEntity.ok(ApiResponse.success("Commune deleted successfully", null));
    }

    // Village
    @PostMapping("/villages/get-by-commune")
    public ResponseEntity<ApiResponse<PaginationResponse<VillageResponseDto>>> getVillagesByCommune(@RequestParam String communeCode, @RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Villages retrieved successfully", addressService.getVillagesByCommune(request, communeCode)));
    }

    @PostMapping("/villages/get-by-code/{code}")
    public ResponseEntity<ApiResponse<VillageResponseDto>> getVillageByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("Village retrieved successfully", addressService.getVillageByCode(code)));
    }

    @PostMapping("/villages/create")
    public ResponseEntity<ApiResponse<VillageResponseDto>> createVillage(@RequestBody VillageRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Village created successfully", addressService.createVillage(request)));
    }

    @PostMapping("/villages/update/{code}")
    public ResponseEntity<ApiResponse<VillageResponseDto>> updateVillage(@PathVariable String code, @RequestBody VillageRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Village updated successfully", addressService.updateVillage(code, request)));
    }

    @PostMapping("/villages/delete/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteVillage(@PathVariable String code) {
        addressService.deleteVillage(code);
        return ResponseEntity.ok(ApiResponse.success("Village deleted successfully", null));
    }
}
