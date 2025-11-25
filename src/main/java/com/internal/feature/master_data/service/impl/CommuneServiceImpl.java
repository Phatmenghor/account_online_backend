package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.CommuneRequestDto;
import com.internal.feature.master_data.dto.response.CommuneResponseDto;
import com.internal.feature.master_data.mapper.CommuneMapper;
import com.internal.feature.master_data.models.Commune;
import com.internal.feature.master_data.models.District;
import com.internal.feature.master_data.repository.CommuneRepository;
import com.internal.feature.master_data.repository.DistrictRepository;
import com.internal.feature.master_data.service.CommuneService;
import com.internal.feature.master_data.specification.CommuneSpec;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommuneServiceImpl implements CommuneService {

    private final CommuneRepository communeRepository;
    private final DistrictRepository districtRepository;
    private final CommuneMapper communeMapper;

    @Override
    public PaginationResponse<CommuneResponseDto> getAllCommunes(AllMasterDataRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<Commune> spec = CommuneSpec.searchByName(request.getSearch());

        Page<Commune> page = communeRepository.findAll(spec, pageable);
        List<CommuneResponseDto> content = communeMapper.toDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public PaginationResponse<CommuneResponseDto> getCommunesByDistrict(AllMasterDataRequest request, String districtCode) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<Commune> spec = CommuneSpec.searchByName(request.getSearch())
                .and((root, query, cb) -> cb.equal(root.get("district").get("districtCode"), districtCode));

        Page<Commune> page = communeRepository.findAll(spec, pageable);
        List<CommuneResponseDto> content = communeMapper.toDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public CommuneResponseDto getCommuneById(Long id) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commune not found with id: " + id));
        return communeMapper.toDto(commune);
    }

    @Override
    @Transactional
    public CommuneResponseDto createCommune(CommuneRequestDto request) {
        if (communeRepository.existsByCommuneCode(request.getCommuneCode())) {
            throw new RuntimeException("Commune with code " + request.getCommuneCode() + " already exists");
        }
        District district = districtRepository.findByDistrictCode(request.getDistrictCode())
                .orElseThrow(() -> new NotFoundException("District not found with code: " + request.getDistrictCode()));

        Commune commune = communeMapper.fromCreateDto(request);
        commune.setDistrict(district);
        return communeMapper.toDto(communeRepository.save(commune));
    }

    @Override
    @Transactional
    public CommuneResponseDto updateCommune(Long id, CommuneRequestDto request) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commune not found with id: " + id));

        if (request.getDistrictCode() != null && !commune.getDistrict().getDistrictCode().equals(request.getDistrictCode())) {
            District district = districtRepository.findByDistrictCode(request.getDistrictCode())
                    .orElseThrow(() -> new NotFoundException("District not found with code: " + request.getDistrictCode()));
            commune.setDistrict(district);
        }

        communeMapper.updateFromDto(request, commune);
        return communeMapper.toDto(communeRepository.save(commune));
    }

    @Override
    @Transactional
    public void deleteCommune(Long id) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commune not found with id: " + id));
        communeRepository.delete(commune);
    }
}
