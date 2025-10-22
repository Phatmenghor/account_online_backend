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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferenceDocServiceImpl implements ReferenceDocService {
    private final ReferenceDocRepository repository;
    private final ReferenceDocMapper mapper;
    @Override
    public ReferenceDocDto getById(Long id) {
        ReferenceDoc doc = repository.findById(id)
                .orElseThrow(()-> new NotFoundException("Bank not found"));
        return mapper.toDto(doc);
    }

    @Override
    public AllReferenceDocResponseDto getAll(GetAllReferenceDocRequest request) {
        // Create Pageable object
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        // Fetch paginated data from repository
        Page<ReferenceDoc> page = repository.findAll(pageable);

        // Filter and map to DTO using MapStruct
        List<ReferenceDocDto> content = page.stream()
                .filter(doc -> request.getStatus() == null || doc.getStatus() == request.getStatus())
                .filter(doc -> request.getSearch() == null ||
                        doc.getNameEn().toLowerCase().contains(request.getSearch().toLowerCase()) ||
                        doc.getNameKh().toLowerCase().contains(request.getSearch().toLowerCase()))
                .map(doc -> mapper.toDto(doc))
                .collect(Collectors.toList());

        // Map to response including pagination metadata
        return mapper.mapToListDto(content, page);
    }

    @Override
    public ReferenceDocDto create(ReferenceDocCreateRequestDto request) {
        if (repository.existsByNameEn(request.getNameEn())) {
            throw new DuplicateNameException("doc with English name '" + request.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(request.getNameKh())) {
            throw new DuplicateNameException("doc with Khmer name '" + request.getNameKh() + "' already exists");
        }

        ReferenceDoc doc = mapper.fromCreateDto(request);
        repository.save(doc);
        return mapper.toDto(doc);
    }

    @Override
    public ReferenceDocDto update(Long id, ReferenceDocUpdateRequestDto request) {
        ReferenceDoc doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doc not found"));

        mapper.updateFromDto(request, doc);

        repository.save(doc);
        return mapper.toDto(doc);
    }

    @Override
    public void delete(Long id) {
      repository.deleteById(id);
    }
}
