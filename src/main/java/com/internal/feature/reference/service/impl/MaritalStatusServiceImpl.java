package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.custom.DuplicateNameException;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllMaritalStatusRequest;
import com.internal.feature.reference.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.reference.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllMaritalStatusResponseDto;
import com.internal.feature.reference.dto.response.MaritalStatusDto;
import com.internal.feature.reference.mapper.MaritalStatusMapper;
import com.internal.feature.reference.models.MaritalStatus;
import com.internal.feature.reference.repository.MaritalStatusRepository;
import com.internal.feature.reference.service.MaritalStatusService;
import com.internal.feature.master_data.specification.MaritalStatusSpec;
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
public class MaritalStatusServiceImpl implements MaritalStatusService {

    private final MaritalStatusRepository repository;
    private final MaritalStatusMapper mapper;

    @Override
    public MaritalStatusDto getById(Long id) {
        MaritalStatus status = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Marital status not found"));
        return mapper.toDto(status); // null = return both languages
    }

    @Override
    public AllMaritalStatusResponseDto getAll(GetAllMaritalStatusRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        // Build specification dynamically
        var spec = MaritalStatusSpec.hasStatus(request.getStatus())
                .and(MaritalStatusSpec.searchByName(request.getSearch()));

        Page<MaritalStatus> page = repository.findAll(spec, pageable);

        List<MaritalStatusDto> content = page.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return mapper.mapToListDto(content, page);
    }


    @Override
    public MaritalStatusDto create(MaritalStatusCreateRequestDto request) {
        if (repository.existsByNameEn(request.getNameEn())) {
            throw new DuplicateNameException("Marital status with English name '" + request.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(request.getNameKh())) {
            throw new DuplicateNameException("Marital status with Khmer name '" + request.getNameKh() + "' already exists");
        }

        MaritalStatus status = mapper.fromCreateDto(request);
        repository.save(status);
        return mapper.toDto(status);
    }

    @Override
    public MaritalStatusDto update(Long id, MaritalStatusUpdateRequestDto request) {
        MaritalStatus status = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Marital status not found"));

        mapper.updateFromDto(request, status);

        repository.save(status);
        return mapper.toDto(status);
    }

    @Override
    public MaritalStatusDto delete(Long id) {
        log.info("Deleting Marital status with id: {}", id);

        MaritalStatus maritalStatus = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Marital status id " + id + " could not be found"));

        repository.deleteById(id);

        return mapper.toDto(maritalStatus);
    }
}
