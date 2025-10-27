package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllReferenceRequest;
import com.internal.feature.reference.dto.request.ReferenceCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllReferenceResponseDto;
import com.internal.feature.reference.dto.response.ReferenceDto;
import com.internal.feature.reference.mapper.ReferenceMapper;
import com.internal.feature.reference.models.Reference;
import com.internal.feature.reference.repository.ReferenceRepository;
import com.internal.feature.reference.service.ReferenceService;
import com.internal.feature.master_data.specification.ReferenceSpec;
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
public class ReferenceServiceImpl implements ReferenceService {

    private final ReferenceRepository repository;
    private final ReferenceMapper mapper;

    @Override
    public ReferenceDto getById(Long id) {
        Reference bank = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reference not found"));
        return mapper.toDto(bank);
    }

    @Override
    public AllReferenceResponseDto getAll(GetAllReferenceRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        // Build specification dynamically
        var spec = ReferenceSpec.hasStatus(request.getStatus())
                .and(ReferenceSpec.searchByName(request.getSearch()));

        Page<Reference> page = repository.findAll(spec, pageable);

        List<ReferenceDto> content = page.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return mapper.mapToListDto(content, page);
    }

    @Override
    public ReferenceDto create(ReferenceCreateRequestDto request) {
        if (repository.existsByNameEn(request.getNameEn())) {
            throw new DuplicateNameException("Bank with English name '" + request.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(request.getNameKh())) {
            throw new DuplicateNameException("Bank with Khmer name '" + request.getNameKh() + "' already exists");
        }

        Reference bank = mapper.fromCreateDto(request);
        repository.save(bank);
        return mapper.toDto(bank);
    }

    @Override
    public ReferenceDto update(Long id, ReferenceUpdateRequestDto request) {
        Reference bank = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found"));

        mapper.updateFromDto(request, bank);

        repository.save(bank);
        return mapper.toDto(bank);
    }

    @Override
    public ReferenceDto delete(Long id) {
        log.info("Deleting reference with id: {}", id);

        Reference reference = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reference id " + id + " could not be found"));

        repository.deleteById(id);
        return mapper.toDto(reference);
    }
}
