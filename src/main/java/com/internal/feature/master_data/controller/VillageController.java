package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.VillageRequestDto;
import com.internal.feature.master_data.dto.response.VillageResponseDto;
import com.internal.feature.master_data.service.VillageService;
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
@Tag(name = "Village Management")
public class VillageController {

    private final VillageService villageService;

    @PostMapping("/villages/all")
    public ResponseEntity<ApiResponse<PaginationResponse<VillageResponseDto>>> getAllVillages(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success("All villages retrieved successfully", villageService.getAllVillages(request)));
    }

    @PostMapping("/villages/get-by-id/{id}")
    public ResponseEntity<ApiResponse<VillageResponseDto>> getVillageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Village retrieved successfully", villageService.getVillageById(id)));
    }

    @PostMapping("/villages/create")
    public ResponseEntity<ApiResponse<VillageResponseDto>> createVillage(@RequestBody VillageRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Village created successfully", villageService.createVillage(request)));
    }

    @PostMapping("/villages/update/{id}")
    public ResponseEntity<ApiResponse<VillageResponseDto>> updateVillage(@PathVariable Long id, @RequestBody VillageRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Village updated successfully", villageService.updateVillage(id, request)));
    }

    @PostMapping("/villages/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteVillage(@PathVariable Long id) {
        villageService.deleteVillage(id);
        return ResponseEntity.ok(ApiResponse.success("Village deleted successfully", null));
    }
}
