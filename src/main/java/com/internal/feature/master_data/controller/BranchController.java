package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.BranchRequestDto;
import com.internal.feature.master_data.dto.response.BranchResponseDto;
import com.internal.feature.master_data.service.BranchService;
import com.internal.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data/branches")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Branch Management")
public class BranchController {

    private final BranchService branchService;

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BranchResponseDto>>> getAllBranches(@RequestBody AllMasterDataRequest request) {
        log.info("Fetching all branches");
        PaginationResponse<BranchResponseDto> branches = branchService.getAllBranches(request);
        return ResponseEntity.ok(ApiResponse.success("All branches retrieved successfully", branches));
    }

    @PostMapping("/get-by-code/{code}")
    public ResponseEntity<ApiResponse<BranchResponseDto>> getBranchByCode(@PathVariable String code) {
        log.info("Fetching branch with code: {}", code);
        BranchResponseDto branch = branchService.getBranchByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Branch retrieved successfully", branch));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BranchResponseDto>> createBranch(@RequestBody BranchRequestDto request) {
        log.info("Creating new branch: {}", request);
        BranchResponseDto branch = branchService.createBranch(request);
        return ResponseEntity.ok(ApiResponse.success("Branch created successfully", branch));
    }

    @PostMapping("/update/{code}")
    public ResponseEntity<ApiResponse<BranchResponseDto>> updateBranch(@PathVariable String code, @RequestBody BranchRequestDto request) {
        log.info("Updating branch with code: {}", code);
        BranchResponseDto branch = branchService.updateBranch(code, request);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", branch));
    }

    @PostMapping("/delete/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable String code) {
        log.info("Deleting branch with code: {}", code);
        branchService.deleteBranch(code);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully", null));
    }
}
