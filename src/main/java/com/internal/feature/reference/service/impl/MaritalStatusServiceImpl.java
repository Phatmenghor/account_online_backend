package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllMaritalStatusRequest;
import com.internal.feature.reference.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.reference.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.reference.dto.response.MaritalStatusDto;
import com.internal.feature.reference.models.MaritalStatus;
import com.internal.feature.reference.repository.MaritalStatusRepository;
import com.internal.feature.reference.service.MaritalStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaritalStatusServiceImpl implements MaritalStatusService {

    private final MaritalStatusRepository repository;

    @Override
    public MaritalStatusDto getById(Long id) {
        MaritalStatus status = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Marital status not found"));
        return toDto(status);
    }

    @Override
    public List<MaritalStatusDto> getAll(GetAllMaritalStatusRequest request) {
        return repository.findAll().stream()
                // Filter by status if provided
                .filter(status -> request.getStatus() == null || status.getStatus() == request.getStatus())
                // Filter by search if provided
                .filter(status -> request.getSearch() == null ||
                        status.getNameEn().toLowerCase().contains(request.getSearch().toLowerCase()) ||
                        status.getNameKh().toLowerCase().contains(request.getSearch().toLowerCase()))
                // Map to DTO considering language filter
                .map(status -> {
                    MaritalStatusDto dto = new MaritalStatusDto();
                    dto.setId(status.getId());

                    if ("en".equalsIgnoreCase(request.getLanguage())) {
                        dto.setNameEn(status.getNameEn());
                        dto.setNameKh(null);
                    } else if ("kh".equalsIgnoreCase(request.getLanguage())) {
                        dto.setNameEn(null);
                        dto.setNameKh(status.getNameKh());
                    } else {
                        dto.setNameEn(status.getNameEn());
                        dto.setNameKh(status.getNameKh());
                    }

                    dto.setStatus(status.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public MaritalStatusDto create(MaritalStatusCreateRequestDto request) {
        // Check for duplicate names
        if (repository.existsByNameEn(request.getNameEn())) {
            throw new DuplicateNameException("Marital status with English name '" + request.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(request.getNameKh())) {
            throw new DuplicateNameException("Marital status with Khmer name '" + request.getNameKh() + "' already exists");
        }

        MaritalStatus status = new MaritalStatus();
        status.setNameEn(request.getNameEn());
        status.setNameKh(request.getNameKh());
        status.setStatus(request.getStatus());
        repository.save(status);

        return toDto(status);
    }

    @Override
    public MaritalStatusDto update(Long id, MaritalStatusUpdateRequestDto request) {
        MaritalStatus status = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Marital status not found"));

        // Update only if the field is provided in the request
        if (request.getNameEn() != null && !request.getNameEn().equals(status.getNameEn())) {
            if (repository.existsByNameEn(request.getNameEn())) {
                throw new DuplicateNameException("Marital status with English name '" + request.getNameEn() + "' already exists");
            }
            status.setNameEn(request.getNameEn());
        }

        if (request.getNameKh() != null && !request.getNameKh().equals(status.getNameKh())) {
            if (repository.existsByNameKh(request.getNameKh())) {
                throw new DuplicateNameException("Marital status with Khmer name '" + request.getNameKh() + "' already exists");
            }
            status.setNameKh(request.getNameKh());
        }

        if (request.getStatus() != null) {
            status.setStatus(request.getStatus());
        }

        repository.save(status);
        return toDto(status);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private MaritalStatusDto toDto(MaritalStatus status) {
        return new MaritalStatusDto(
                status.getId(),
                status.getNameEn(),
                status.getNameKh(),
                status.getStatus()
        );
    }
}
