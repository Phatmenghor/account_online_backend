package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllMaritalStatusRequest;
import com.internal.feature.reference.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.reference.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllMaritalStatusResponseDto;
import com.internal.feature.reference.dto.response.MaritalStatusDto;
import com.internal.feature.reference.mapper.MaritalStatusMapper;
import com.internal.feature.reference.models.MaritalStatus;
import com.internal.feature.reference.repository.MaritalStatusRepository;
import com.internal.feature.reference.service.MaritalStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaritalStatusServiceImpl implements MaritalStatusService {

    private final MaritalStatusRepository repository;
    private final MaritalStatusMapper mapper;

    @Override
    public MaritalStatusDto getById(Long id) {
        MaritalStatus status = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Marital status not found"));
        return mapper.toDto(status, null); // null = return both languages
    }

    @Override
    public AllMaritalStatusResponseDto getAll(GetAllMaritalStatusRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        Page<MaritalStatus> page = repository.findAll(pageable);

        // Optional filtering: status + search
        List<MaritalStatusDto> content = page.stream()
                .filter(status -> request.getStatus() == null || status.getStatus() == request.getStatus())
                .filter(status -> request.getSearch() == null ||
                        status.getNameEn().toLowerCase().contains(request.getSearch().toLowerCase()) ||
                        status.getNameKh().toLowerCase().contains(request.getSearch().toLowerCase()))
                .map(status -> mapper.toDto(status, request.getLanguage()))
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
        return mapper.toDto(status, null);
    }

    @Override
    public MaritalStatusDto update(Long id, MaritalStatusUpdateRequestDto request) {
        MaritalStatus status = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Marital status not found"));

        mapper.updateFromDto(request, status);

        repository.save(status);
        return mapper.toDto(status, null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
