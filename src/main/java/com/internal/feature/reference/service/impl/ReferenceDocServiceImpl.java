package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllReferenceDocRequest;
import com.internal.feature.reference.dto.request.ReferenceDocCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceDocUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllReferenceDocResponseDto;
import com.internal.feature.reference.dto.response.ReferenceDocDto;
import com.internal.feature.reference.mapper.ReferenceDocMapper;
import com.internal.feature.reference.models.ReferenceDoc;
import com.internal.feature.reference.repository.ReferenceDocRepository;
import com.internal.feature.reference.service.ReferenceDocService;
import com.internal.feature.master_data.specification.ReferenceDocSpec;
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
public class ReferenceDocServiceImpl implements ReferenceDocService {
    private final ReferenceDocRepository repository;
    private final ReferenceDocMapper mapper;
    @Override
    public ReferenceDocDto getById(Long id) {
        ReferenceDoc doc = repository.findById(id)
                .orElseThrow(()-> new NotFoundException("Doc reference with id "+id+" not found"));
        return mapper.toDto(doc);
    }

    @Override
    public AllReferenceDocResponseDto getAllReferenceDoc(GetAllReferenceDocRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        var spec = ReferenceDocSpec.hasStatus(request.getStatus())
                .and(ReferenceDocSpec.searchByName(request.getSearch()));

        Page<ReferenceDoc> page = repository.findAll(spec,pageable);
        List<ReferenceDocDto> content = page.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        // Map to response including pagination metadata
        return mapper.mapToListDto(content, page);
    }

    @Override
    public ReferenceDocDto create(ReferenceDocCreateRequestDto request) {
        if (repository.existsByNameEn(request.getNameEn())) {
            throw new DuplicateNameException("Doc reference  with English name '" + request.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(request.getNameKh())) {
            throw new DuplicateNameException("Doc reference with Khmer name '" + request.getNameKh() + "' already exists");
        }

        ReferenceDoc doc = mapper.fromCreateDto(request);
        repository.save(doc);
        return mapper.toDto(doc);
    }

    @Override
    public ReferenceDocDto update(Long id, ReferenceDocUpdateRequestDto request) {
        ReferenceDoc doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doc reference with ID "+id+" not found"));

        mapper.updateFromDto(request, doc);

        repository.save(doc);
        return mapper.toDto(doc);
    }

    @Override
    public ReferenceDocDto delete(Long id) {
        log.info("Deleting doc reference with id: {}", id);

        ReferenceDoc doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doc reference id " + id + " could not be found"));

        repository.deleteById(id);
        return mapper.toDto(doc);
    }
}
