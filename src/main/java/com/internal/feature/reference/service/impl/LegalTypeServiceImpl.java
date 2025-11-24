package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.custom.DuplicateNameException;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllLegalTypeRequest;
import com.internal.feature.reference.dto.request.LegalTypeCreateRequestDto;
import com.internal.feature.reference.dto.request.LegalTypeUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllLegalTypeResponseDto;
import com.internal.feature.reference.dto.response.LegalTypeDto;
import com.internal.feature.reference.mapper.LegalTypeMapper;
import com.internal.feature.reference.models.LegalType;
import com.internal.feature.reference.repository.LegalTypeRepository;
import com.internal.feature.reference.service.LegalTypeService;
import com.internal.feature.master_data.specification.LegalTypeSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegalTypeServiceImpl implements LegalTypeService {
    private final LegalTypeRepository repository;
    private final LegalTypeMapper mapper;

    @Override
    public LegalTypeDto getById(Long id) {
        LegalType doc = repository.findById(id)
                .orElseThrow(()-> new NotFoundException("Legal type with id "+id+" not found"));
        return mapper.toDto(doc);
    }

    @Override
    public AllLegalTypeResponseDto getAllLegalType(GetAllLegalTypeRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        var spec = LegalTypeSpec.hasStatus(request.getStatus())
                .and(LegalTypeSpec.searchByName(request.getSearch()));

        Page<LegalType> page = repository.findAll(spec,pageable);
        List<LegalTypeDto> content = page.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        // Map to response including pagination metadata
        return mapper.mapToListDto(content, page);
    }

    @Override
    public List<LegalTypeDto> getAllLegalTypePublic(String search) {
        // FORCE ACTIVE STATUS
        var spec = LegalTypeSpec.hasStatus(com.internal.enumation.StatusData.ACTIVE)
                .and(LegalTypeSpec.searchByName(search));

        List<LegalType> list = repository.findAll(spec);
        return mapper.toDtoList(list);
    }

    @Override
    public LegalTypeDto create(LegalTypeCreateRequestDto request) {
        if (repository.existsByNameEn(request.getNameEn())) {
            throw new DuplicateNameException("Legal type with English name '" + request.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(request.getNameKh())) {
            throw new DuplicateNameException("Legal type with Khmer name '" + request.getNameKh() + "' already exists");
        }

        LegalType doc = mapper.fromCreateDto(request);
        repository.save(doc);
        return mapper.toDto(doc);
    }

    @Override
    public LegalTypeDto update(Long id, LegalTypeUpdateRequestDto request) {
        LegalType doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Legal type with ID " + id + " not found"));

        mapper.updateFromDto(request, doc);

        repository.save(doc);
        return mapper.toDto(doc);
    }

    @Override
    public LegalTypeDto delete(Long id) {
        log.info("Deleting legal type with id: {}", id);

        LegalType doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Legal type with id " + id + " could not be found"));

        repository.deleteById(id);
        return mapper.toDto(doc);
    }
}
