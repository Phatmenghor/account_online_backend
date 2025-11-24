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

    @PostMapping("/provinces/get-by-id/{id}")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> getProvinceById( @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Province retrieved successfully", addressService.getProvinceById(id)));
    }

    @PostMapping("/provinces/create")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> createProvince(@RequestBody ProvinceRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Province created successfully", addressService.createProvince(request)));
    }

    @PostMapping("/provinces/update/{id}")
    public ResponseEntity<ApiResponse<ProvinceResponseDto>> updateProvince(@PathVariable Long id, @RequestBody ProvinceRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Province updated successfully", addressService.updateProvince(id, request)));
    }

    @PostMapping("/provinces/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProvince(@PathVariable Long id) {
        addressService.deleteProvince(id);
        return ResponseEntity.ok(ApiResponse.success("Province deleted successfully", null));
    }

    // District
    @PostMapping("/districts/all")
    public ResponseEntity<ApiResponse<PaginationResponse<DistrictResponseDto>>> getAllDistricts(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("All districts retrieved successfully", addressService.getAllDistricts(request)));
    }

    @PostMapping("/districts/get-by-id/{id}")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> getDistrictById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("District retrieved successfully", addressService.getDistrictById(id)));
    }

    @PostMapping("/districts/create")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> createDistrict(@RequestBody DistrictRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("District created successfully", addressService.createDistrict(request)));
    }

    @PostMapping("/districts/update/{id}")
    public ResponseEntity<ApiResponse<DistrictResponseDto>> updateDistrict(@PathVariable Long id, @RequestBody DistrictRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("District updated successfully", addressService.updateDistrict(id, request)));
    }

    @PostMapping("/districts/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDistrict(@PathVariable Long id) {
        addressService.deleteDistrict(id);
        return ResponseEntity.ok(ApiResponse.success("District deleted successfully", null));
    }

    // Commune
    @PostMapping("/communes/all")
    public ResponseEntity<ApiResponse<PaginationResponse<CommuneResponseDto>>> getAllCommunes(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("All communes retrieved successfully", addressService.getAllCommunes(request)));
    }

    @PostMapping("/communes/get-by-id/{id}")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> getCommuneById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Commune retrieved successfully", addressService.getCommuneById(id)));
    }

    @PostMapping("/communes/create")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> createCommune(@RequestBody CommuneRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Commune created successfully", addressService.createCommune(request)));
    }

    @PostMapping("/communes/update/{id}")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> updateCommune(@PathVariable Long id, @RequestBody CommuneRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Commune updated successfully", addressService.updateCommune(id, request)));
    }

    @PostMapping("/communes/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCommune(@PathVariable Long id) {
        addressService.deleteCommune(id);
        return ResponseEntity.ok(ApiResponse.success("Commune deleted successfully", null));
    }

    // Village`
    @PostMapping("/villages/all")
    public ResponseEntity<ApiResponse<PaginationResponse<VillageResponseDto>>> getAllVillages(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("All villages retrieved successfully", addressService.getAllVillages(request)));
    }

    @PostMapping("/villages/get-by-id/{id}")
    public ResponseEntity<ApiResponse<VillageResponseDto>> getVillageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Village retrieved successfully", addressService.getVillageById(id)));
    }

    @PostMapping("/villages/create")
    public ResponseEntity<ApiResponse<VillageResponseDto>> createVillage(@RequestBody VillageRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Village created successfully", addressService.createVillage(request)));
    }

    @PostMapping("/villages/update/{id}")
    public ResponseEntity<ApiResponse<VillageResponseDto>> updateVillage(@PathVariable Long id, @RequestBody VillageRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Village updated successfully", addressService.updateVillage(id, request)));
    }

    @PostMapping("/villages/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteVillage(@PathVariable Long id) {
        addressService.deleteVillage(id);
        return ResponseEntity.ok(ApiResponse.success("Village deleted successfully", null));
    }
}
