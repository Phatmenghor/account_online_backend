package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllReferenceBankRequest;
import com.internal.feature.reference.dto.request.ReferenceBankCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceBankUpdateRequestDto;
import com.internal.feature.reference.dto.response.ReferenceBankDto;
import com.internal.feature.reference.models.ReferenceBank;
import com.internal.feature.reference.repository.ReferenceBankRepository;
import com.internal.feature.reference.service.ReferenceBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferenceBankServiceImpl implements ReferenceBankService {

    private final ReferenceBankRepository repository;

    @Override
    public ReferenceBankDto getById(Long id) {
        ReferenceBank bank = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found"));
        return toDto(bank);
    }

    @Override
    public List<ReferenceBankDto> getAll(GetAllReferenceBankRequest request) {
        return repository.findAll().stream()
                .filter(bank -> request.getStatus() == null || bank.getStatus() == request.getStatus())
                .filter(bank -> request.getSearch() == null ||
                        bank.getNameEn().toLowerCase().contains(request.getSearch().toLowerCase()) ||
                        bank.getNameKh().toLowerCase().contains(request.getSearch().toLowerCase()))
                .map(bank -> {
                    ReferenceBankDto dto = new ReferenceBankDto();
                    dto.setId(bank.getId());

                    // Apply language filter
                    if ("en".equalsIgnoreCase(request.getLanguage())) {
                        dto.setNameEn(bank.getNameEn());
                        dto.setNameKh(null);
                    } else if ("kh".equalsIgnoreCase(request.getLanguage())) {
                        dto.setNameEn(null);
                        dto.setNameKh(bank.getNameKh());
                    } else {
                        dto.setNameEn(bank.getNameEn());
                        dto.setNameKh(bank.getNameKh());
                    }

                    dto.setStatus(bank.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ReferenceBankDto create(ReferenceBankCreateRequestDto request) {
        // Check for duplicates
        if (repository.existsByNameEn(request.getNameEn())) {
            throw new DuplicateNameException("Bank with English name '" + request.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(request.getNameKh())) {
            throw new DuplicateNameException("Bank with Khmer name '" + request.getNameKh() + "' already exists");
        }

        ReferenceBank bank = new ReferenceBank();
        bank.setNameEn(request.getNameEn());
        bank.setNameKh(request.getNameKh());
        bank.setStatus(request.getStatus());
        repository.save(bank);
        return toDto(bank);
    }

    @Override
    public ReferenceBankDto update(Long id, ReferenceBankUpdateRequestDto request) {
        ReferenceBank bank = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found"));

        // Update English name if provided and check for duplicates
        if (request.getNameEn() != null && !request.getNameEn().equals(bank.getNameEn())) {
            if (repository.existsByNameEn(request.getNameEn())) {
                throw new DuplicateNameException("Bank with English name '" + request.getNameEn() + "' already exists");
            }
            bank.setNameEn(request.getNameEn());
        }

        // Update Khmer name if provided and check for duplicates
        if (request.getNameKh() != null && !request.getNameKh().equals(bank.getNameKh())) {
            if (repository.existsByNameKh(request.getNameKh())) {
                throw new DuplicateNameException("Bank with Khmer name '" + request.getNameKh() + "' already exists");
            }
            bank.setNameKh(request.getNameKh());
        }

        // Update status if provided
        if (request.getStatus() != null) {
            bank.setStatus(request.getStatus());
        }

        repository.save(bank);
        return toDto(bank);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ReferenceBankDto toDto(ReferenceBank bank) {
        return new ReferenceBankDto(
                bank.getId(),
                bank.getNameEn(),
                bank.getNameKh(),
                bank.getStatus()
        );
    }
}
