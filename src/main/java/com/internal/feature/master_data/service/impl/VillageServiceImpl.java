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
public class VillageServiceImpl implements VillageService {

    private final VillageRepository villageRepository;
    private final CommuneRepository communeRepository;
    private final VillageMapper villageMapper;

    @Override
    public PaginationResponse<VillageResponseDto> getAllVillages(AllMasterDataRequest request) {
        log.info("Fetching all villages with search: {}", request.getSearch());
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Village> page = villageRepository.findBySearch(request.getSearch(), pageable);
        List<VillageResponseDto> content = villageMapper.toDtoList(page.getContent());

        log.info("Found {} villages", page.getTotalElements());
        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public PaginationResponse<VillageResponseDto> getVillagesByCommune(AllMasterDataRequest request, String communeCode) {
        log.info("Fetching villages by commune code: {} with search: {}", communeCode, request.getSearch());
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Village> page = villageRepository.findByCommuneCodeAndSearch(communeCode, request.getSearch(), pageable);
        List<VillageResponseDto> content = villageMapper.toDtoList(page.getContent());

        log.info("Found {} villages for commune: {}", page.getTotalElements(), communeCode);
        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public VillageResponseDto getVillageById(Long id) {
        log.info("Fetching village by id: {}", id);
        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Village not found with id: " + id));
        return villageMapper.toDto(village);
    }

    @Override
    @Transactional
    public VillageResponseDto createVillage(VillageRequestDto request) {
        log.info("Creating village with code: {}", request.getVillageCode());
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
        log.info("Updating village with id: {}", id);
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
        log.info("Deleting village with id: {}", id);
        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Village not found with id: " + id));
        villageRepository.delete(village);
    }
}
