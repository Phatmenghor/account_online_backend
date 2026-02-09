package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.BranchRequestDto;
import com.internal.feature.master_data.dto.response.BranchResponseDto;
import com.internal.feature.master_data.mapper.BranchMapper;
import com.internal.feature.master_data.models.Branch;
import com.internal.feature.master_data.repository.BranchRepository;
import com.internal.feature.master_data.service.BranchService;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    public PaginationResponse<BranchResponseDto> getAllBranches(AllMasterDataRequest request) {
        log.info("Fetching all branches with search: {}", request.getSearch());
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Branch> page = branchRepository.findBySearch(request.getSearch(), pageable);
        List<BranchResponseDto> content = branchMapper.toDtoList(page.getContent());

        log.info("Found {} branches", page.getTotalElements());
        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public BranchResponseDto getBranchById(Long id) {
        log.info("Fetching branch by id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));
        return branchMapper.toDto(branch);
    }

    @Override
    @Transactional
    public BranchResponseDto createBranch(BranchRequestDto request) {
        log.info("Creating branch with code: {}", request.getBranchCode());
        if (branchRepository.existsByBranchCode(request.getBranchCode())) {
            throw new RuntimeException("Branch with code " + request.getBranchCode() + " already exists");
        }
        Branch branch = branchMapper.fromCreateDto(request);
        return branchMapper.toDto(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchResponseDto updateBranch(Long id, BranchRequestDto request) {
        log.info("Updating branch with id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));
        branchMapper.updateFromDto(request, branch);
        return branchMapper.toDto(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        log.info("Deleting branch with id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));
        branchRepository.delete(branch);
    }
}
