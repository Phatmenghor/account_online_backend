package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.ProvinceRequestDto;
import com.internal.feature.master_data.dto.response.ProvinceResponseDto;
import com.internal.feature.master_data.mapper.ProvinceMapper;
import com.internal.feature.master_data.models.Province;
import com.internal.feature.master_data.repository.ProvinceRepository;
import com.internal.feature.master_data.service.ProvinceService;
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
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceRepository provinceRepository;
    private final ProvinceMapper provinceMapper;

    @Override
    public PaginationResponse<ProvinceResponseDto> getAllProvinces(AllMasterDataRequest request) {
        log.info("Fetching all provinces with search: {}", request.getSearch());
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Province> page = provinceRepository.findBySearch(request.getSearch(), pageable);
        List<ProvinceResponseDto> content = provinceMapper.toDtoList(page.getContent());

        log.info("Found {} provinces", page.getTotalElements());
        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public ProvinceResponseDto getProvinceById(Long id) {
        log.info("Fetching province by id: {}", id);
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Province not found with id: " + id));
        return provinceMapper.toDto(province);
    }

    @Override
    @Transactional
    public ProvinceResponseDto createProvince(ProvinceRequestDto request) {
        log.info("Creating province with code: {}", request.getProvinceCode());
        if (provinceRepository.existsByProvinceCode(request.getProvinceCode())) {
            throw new RuntimeException("Province with code " + request.getProvinceCode() + " already exists");
        }
        Province province = provinceMapper.fromCreateDto(request);
        return provinceMapper.toDto(provinceRepository.save(province));
    }

    @Override
    @Transactional
    public ProvinceResponseDto updateProvince(Long id, ProvinceRequestDto request) {
        log.info("Updating province with id: {}", id);
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Province not found with id: " + id));
        provinceMapper.updateFromDto(request, province);
        return provinceMapper.toDto(provinceRepository.save(province));
    }

    @Override
    @Transactional
    public void deleteProvince(Long id) {
        log.info("Deleting province with id: {}", id);
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Province not found with id: " + id));
        provinceRepository.delete(province);
    }
}
