package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.DistrictRequestDto;
import com.internal.feature.master_data.dto.response.DistrictResponseDto;
import com.internal.feature.master_data.mapper.DistrictMapper;
import com.internal.feature.master_data.models.District;
import com.internal.feature.master_data.models.Province;
import com.internal.feature.master_data.repository.DistrictRepository;
import com.internal.feature.master_data.repository.ProvinceRepository;
import com.internal.feature.master_data.service.DistrictService;
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
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictMapper districtMapper;

    @Override
    public PaginationResponse<DistrictResponseDto> getAllDistricts(AllMasterDataRequest request) {
        log.info("Fetching all districts with search: {}", request.getSearch());
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<District> page = districtRepository.findBySearch(request.getSearch(), pageable);
        List<DistrictResponseDto> content = districtMapper.toDtoList(page.getContent());

        log.info("Found {} districts", page.getTotalElements());
        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public PaginationResponse<DistrictResponseDto> getDistrictsByProvince(AllMasterDataRequest request, String provinceCode) {
        log.info("Fetching districts by province code: {} with search: {}", provinceCode, request.getSearch());
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<District> page = districtRepository.findByProvinceCodeAndSearch(provinceCode, request.getSearch(), pageable);
        List<DistrictResponseDto> content = districtMapper.toDtoList(page.getContent());

        log.info("Found {} districts for province: {}", page.getTotalElements(), provinceCode);
        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public DistrictResponseDto getDistrictById(Long id) {
        log.info("Fetching district by id: {}", id);
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("District not found with id: " + id));
        return districtMapper.toDto(district);
    }

    @Override
    @Transactional
    public DistrictResponseDto createDistrict(DistrictRequestDto request) {
        log.info("Creating district with code: {}", request.getDistrictCode());
        if (districtRepository.existsByDistrictCode(request.getDistrictCode())) {
            throw new RuntimeException("District with code " + request.getDistrictCode() + " already exists");
        }
        Province province = provinceRepository.findByProvinceCode(request.getProvinceCode())
                .orElseThrow(() -> new NotFoundException("Province not found with code: " + request.getProvinceCode()));

        District district = districtMapper.fromCreateDto(request);
        district.setProvince(province);
        return districtMapper.toDto(districtRepository.save(district));
    }

    @Override
    @Transactional
    public DistrictResponseDto updateDistrict(Long id, DistrictRequestDto request) {
        log.info("Updating district with id: {}", id);
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("District not found with id: " + id));

        if (request.getProvinceCode() != null && !district.getProvince().getProvinceCode().equals(request.getProvinceCode())) {
            Province province = provinceRepository.findByProvinceCode(request.getProvinceCode())
                    .orElseThrow(() -> new NotFoundException("Province not found with code: " + request.getProvinceCode()));
            district.setProvince(province);
        }

        districtMapper.updateFromDto(request, district);
        return districtMapper.toDto(districtRepository.save(district));
    }

    @Override
    @Transactional
    public void deleteDistrict(Long id) {
        log.info("Deleting district with id: {}", id);
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("District not found with id: " + id));
        districtRepository.delete(district);
    }
}
