package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.VillageRequestDto;
import com.internal.feature.master_data.dto.response.VillageResponseDto;
import com.internal.feature.master_data.mapper.VillageMapper;
import com.internal.feature.master_data.models.Commune;
import com.internal.feature.master_data.models.Village;
import com.internal.feature.master_data.repository.CommuneRepository;
import com.internal.feature.master_data.repository.VillageRepository;
import com.internal.feature.master_data.service.VillageService;
import com.internal.feature.master_data.specification.VillageSpec;
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
public class VillageServiceImpl implements VillageService {

    private final VillageRepository villageRepository;
    private final CommuneRepository communeRepository;
    private final VillageMapper villageMapper;

    @Override
    public PaginationResponse<VillageResponseDto> getAllVillages(AllMasterDataRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Village> spec = VillageSpec.searchByName(request.getSearch());

        Page<Village> page = villageRepository.findAll(spec, pageable);
        List<VillageResponseDto> content = villageMapper.toDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public PaginationResponse<VillageResponseDto> getVillagesByCommune(AllMasterDataRequest request, String communeCode) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Village> spec = VillageSpec.searchByName(request.getSearch())
                .and((root, query, cb) -> cb.equal(root.get("commune").get("communeCode"), communeCode));

        Page<Village> page = villageRepository.findAll(spec, pageable);
        List<VillageResponseDto> content = villageMapper.toDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public VillageResponseDto getVillageById(Long id) {
        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Village not found with id: " + id));
        return villageMapper.toDto(village);
    }

    @Override
    @Transactional
    public VillageResponseDto createVillage(VillageRequestDto request) {
        if (villageRepository.existsByVillageCode(request.getVillageCode())) {
            throw new RuntimeException("Village with code " + request.getVillageCode() + " already exists");
        }
        Commune commune = communeRepository.findByCommuneCode(request.getCommuneCode())
                .orElseThrow(() -> new NotFoundException("Commune not found with code: " + request.getCommuneCode()));

        Village village = villageMapper.fromCreateDto(request);
        village.setCommune(commune);
        return villageMapper.toDto(villageRepository.save(village));
    }

    @Override
    @Transactional
    public VillageResponseDto updateVillage(Long id, VillageRequestDto request) {
        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Village not found with id: " + id));

        if (request.getCommuneCode() != null && !village.getCommune().getCommuneCode().equals(request.getCommuneCode())) {
            Commune commune = communeRepository.findByCommuneCode(request.getCommuneCode())
                    .orElseThrow(() -> new NotFoundException("Commune not found with code: " + request.getCommuneCode()));
            village.setCommune(commune);
        }

        villageMapper.updateFromDto(request, village);
        return villageMapper.toDto(villageRepository.save(village));
    }

    @Override
    @Transactional
    public void deleteVillage(Long id) {
        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Village not found with id: " + id));
        villageRepository.delete(village);
    }
}
