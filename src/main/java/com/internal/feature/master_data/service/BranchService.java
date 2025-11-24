package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.BranchRequestDto;
import com.internal.feature.master_data.dto.response.BranchResponseDto;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.utils.pagination.PaginationResponse;

import java.util.List;

public interface BranchService {
    PaginationResponse<BranchResponseDto> getAllBranches(AllMasterDataRequest request);
    BranchResponseDto getBranchById(Long id);
    BranchResponseDto createBranch(BranchRequestDto request);
    BranchResponseDto updateBranch(Long id, BranchRequestDto request);
    void deleteBranch(Long id);
}
