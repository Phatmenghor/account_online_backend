package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.BranchRequestDto;
import com.internal.feature.master_data.dto.response.BranchResponseDto;
import com.internal.feature.master_data.models.Branch;
import com.internal.feature.master_data.repository.BranchRepository;
import com.internal.feature.master_data.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.specification.BranchSpec;
import com.internal.utils.pagination.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public PaginationResponse<BranchResponseDto> getAllBranches(AllMasterDataRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<Branch> spec = BranchSpec.searchByName(request.getSearch());

        Page<Branch> page = branchRepository.findAll(spec, pageable);
        List<BranchResponseDto> content = page.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public BranchResponseDto getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));
        return mapToDto(branch);
    }

    @Override
    @Transactional
    public BranchResponseDto createBranch(BranchRequestDto request) {
        if (branchRepository.existsByBranchCode(request.getBranchCode())) {
            throw new RuntimeException("Branch with code " + request.getBranchCode() + " already exists");
        }
        Branch branch = new Branch();
        branch.setBranchCode(request.getBranchCode());
        branch.setBranchKh(request.getBranchKh());
        return mapToDto(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchResponseDto updateBranch(Long id, BranchRequestDto request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));
        branch.setBranchKh(request.getBranchKh());
        return mapToDto(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));
        branchRepository.delete(branch);
    }

    private BranchResponseDto mapToDto(Branch branch) {
        return BranchResponseDto.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchKh(branch.getBranchKh())
                .build();
    }
}
