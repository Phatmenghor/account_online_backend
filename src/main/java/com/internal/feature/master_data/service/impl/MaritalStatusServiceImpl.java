package com.internal.feature.master_data.service.impl;

import com.internal.enumation.StatusData;
import com.internal.exceptions.error.custom.DuplicateNameException;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.GetAllMaritalStatusRequest;
import com.internal.feature.master_data.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.master_data.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllMaritalStatusResponseDto;
import com.internal.feature.master_data.dto.response.MaritalStatusDto;
import com.internal.feature.master_data.mapper.MaritalStatusMapper;
import com.internal.feature.master_data.models.MaritalStatus;
import com.internal.feature.master_data.repository.MaritalStatusRepository;
import com.internal.feature.master_data.service.MaritalStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        log.info("Fetching marital statuses - status: {}, search: {}", request.getStatus(), request.getSearch());

        Page<MaritalStatus> page = repository.findByStatusAndSearch(request.getStatus(), request.getSearch(), pageable);

        List<MaritalStatusDto> content = page.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return mapper.mapToListDto(content, page);
    }

    @Override
    public List<MaritalStatusDto> getAllPublic(String search) {
        List<MaritalStatus> list = repository.findActiveBySearch(StatusData.ACTIVE, search);
        return mapper.toDtoList(list);
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
