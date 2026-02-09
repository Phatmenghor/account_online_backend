package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.BranchRequestDto;
import com.internal.feature.master_data.dto.response.BranchResponseDto;
import com.internal.feature.master_data.service.BranchService;
import com.internal.utils.constants.ResponseMessage;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/master-data/branches")
@RequiredArgsConstructor
@Slf4j
public class BranchController {

    private final BranchService branchService;

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BranchResponseDto>>> getAllBranches(@RequestBody AllMasterDataRequest request) {
        log.info("Fetching all branches");
        PaginationResponse<BranchResponseDto> branches = branchService.getAllBranches(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_BRANCHES), branches));
    }

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDto>> getBranchById(@PathVariable Long id) {
        log.info("Fetching branch with ID: {}", id);
        BranchResponseDto branch = branchService.getBranchById(id);
        log.info("Successfully retrieved branch with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.BRANCH), branch));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BranchResponseDto>> createBranch(@RequestBody BranchRequestDto request) {
        log.info("Creating new branch: {}", request);
        BranchResponseDto branch = branchService.createBranch(request);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.created(ResponseMessage.BRANCH), branch));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDto>> updateBranch(@PathVariable Long id, @RequestBody BranchRequestDto request) {
        log.info("Updating branch with ID: {}", id);
        BranchResponseDto branch = branchService.updateBranch(id, request);
        log.info("Successfully updated branch with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.updated(ResponseMessage.BRANCH), branch));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBranch(@PathVariable Long id) {
        log.info("Deleting branch with ID: {}", id);
        branchService.deleteBranch(id);
        log.info("Successfully deleted branch with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.deleted(ResponseMessage.BRANCH), null));
    }
}
