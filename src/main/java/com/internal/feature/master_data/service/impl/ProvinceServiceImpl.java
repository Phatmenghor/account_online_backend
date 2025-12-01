package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.ProvinceRequestDto;
import com.internal.feature.master_data.dto.response.ProvinceResponseDto;
import com.internal.feature.master_data.mapper.ProvinceMapper;
import com.internal.feature.master_data.models.Province;
import com.internal.feature.master_data.repository.ProvinceRepository;
import com.internal.feature.master_data.service.ProvinceService;
import com.internal.feature.master_data.specification.ProvinceSpec;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceRepository provinceRepository;
    private final ProvinceMapper provinceMapper;

    @Override
    public PaginationResponse<ProvinceResponseDto> getAllProvinces(AllMasterDataRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Province> spec = ProvinceSpec.searchByName(request.getSearch());

        Page<Province> page = provinceRepository.findAll(spec, pageable);
        List<ProvinceResponseDto> content = provinceMapper.toDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public ProvinceResponseDto getProvinceById(Long id) {
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Province not found with id: " + id));
        return provinceMapper.toDto(province);
    }

    @Override
    @Transactional
    public ProvinceResponseDto createProvince(ProvinceRequestDto request) {
        if (provinceRepository.existsByProvinceCode(request.getProvinceCode())) {
            throw new RuntimeException("Province with code " + request.getProvinceCode() + " already exists");
        }
        Province province = provinceMapper.fromCreateDto(request);
        return provinceMapper.toDto(provinceRepository.save(province));
    }

    @Override
    @Transactional
    public ProvinceResponseDto updateProvince(Long id, ProvinceRequestDto request) {
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Province not found with id: " + id));
        provinceMapper.updateFromDto(request, province);
        return provinceMapper.toDto(provinceRepository.save(province));
    }

    @Override
    @Transactional
    public void deleteProvince(Long id) {
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Province not found with id: " + id));
        provinceRepository.delete(province);
    }
}
