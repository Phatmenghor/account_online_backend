package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllReferenceBankRequest;
import com.internal.feature.reference.dto.request.ReferenceBankCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceBankUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllReferenceBankResponseDto;
import com.internal.feature.reference.dto.response.ReferenceBankDto;
import com.internal.feature.reference.mapper.ReferenceBankMapper;
import com.internal.feature.reference.models.ReferenceBank;
import com.internal.feature.reference.repository.ReferenceBankRepository;
import com.internal.feature.reference.service.ReferenceBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferenceBankServiceImpl implements ReferenceBankService {

    private final ReferenceBankRepository repository;
    private final ReferenceBankMapper mapper;

    @Override
    public ReferenceBankDto getById(Long id) {
        ReferenceBank bank = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found"));
        return mapper.toDto(bank, null);
    }

    @Override
    public AllReferenceBankResponseDto getAll(GetAllReferenceBankRequest request) {
        // Create Pageable object
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        // Fetch paginated data from repository
        Page<ReferenceBank> page = repository.findAll(pageable);

        // Filter and map to DTO using MapStruct
        List<ReferenceBankDto> content = page.stream()
                .filter(bank -> request.getStatus() == null || bank.getStatus() == request.getStatus())
                .filter(bank -> request.getSearch() == null ||
                        bank.getNameEn().toLowerCase().contains(request.getSearch().toLowerCase()) ||
                        bank.getNameKh().toLowerCase().contains(request.getSearch().toLowerCase()))
                .map(bank -> mapper.toDto(bank, request.getLanguage()))
                .collect(Collectors.toList());

        // Map to response including pagination metadata
        return mapper.mapToListDto(content, page);
    }


    @Override
    public ReferenceBankDto create(ReferenceBankCreateRequestDto request) {
        if (repository.existsByNameEn(request.getNameEn())) {
            throw new DuplicateNameException("Bank with English name '" + request.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(request.getNameKh())) {
            throw new DuplicateNameException("Bank with Khmer name '" + request.getNameKh() + "' already exists");
        }

        ReferenceBank bank = mapper.fromCreateDto(request);
        repository.save(bank);
        return mapper.toDto(bank, null);
    }

    @Override
    public ReferenceBankDto update(Long id, ReferenceBankUpdateRequestDto request) {
        ReferenceBank bank = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found"));

        mapper.updateFromDto(request, bank);

        repository.save(bank);
        return mapper.toDto(bank, null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
