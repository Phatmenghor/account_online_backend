package com.internal.feature.reference.service.impl;

import com.internal.enumation.LanguageEnum;
import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllOccupationRequest;
import com.internal.feature.reference.dto.request.OccupationCreateRequestDto;
import com.internal.feature.reference.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.reference.dto.response.OccupationDto;
import com.internal.feature.reference.models.Occupation;
import com.internal.feature.reference.repository.OccupationRepository;
import com.internal.feature.reference.service.OccupationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OccupationServiceImpl implements OccupationService {

    private final OccupationRepository repository;

    @Override
    public OccupationDto getOccupationById(Long id) {
        Occupation occupation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Occupation not found"));
        return toDto(occupation);
    }

    @Override
    public List<OccupationDto> getAllOccupations(GetAllOccupationRequest request) {
        return repository.findAll().stream()
                // Filter by status if provided
                .filter(occ -> request.getStatus() == null || occ.getStatus() == request.getStatus())
                // Filter by search if provided
                .filter(occ -> request.getSearch() == null ||
                        occ.getNameEn().toLowerCase().contains(request.getSearch().toLowerCase()) ||
                        occ.getNameKh().toLowerCase().contains(request.getSearch().toLowerCase()))
                // Map to DTO considering language filter
                .map(occ -> {
                    OccupationDto dto = new OccupationDto();
                    dto.setId(occ.getId());

                    LanguageEnum lang = request.getLanguage(); // Now using enum

                    if (LanguageEnum.EN.equals(lang)) {
                        dto.setNameEn(occ.getNameEn());
                    } else if (LanguageEnum.KH.equals(lang)) {
                        dto.setNameKh(occ.getNameKh());
                    } else { // null or unspecified
                        dto.setNameEn(occ.getNameEn());
                        dto.setNameKh(occ.getNameKh());
                    }

                    dto.setStatus(occ.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public OccupationDto createOccupation(OccupationCreateRequestDto requestDto) {
        // Check for duplicate names
        if (repository.existsByNameEn(requestDto.getNameEn())) {
            throw new DuplicateNameException("Occupation with English name '" + requestDto.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(requestDto.getNameKh())) {
            throw new DuplicateNameException("Occupation with Khmer name '" + requestDto.getNameKh() + "' already exists");
        }

        Occupation occupation = new Occupation();
        occupation.setNameEn(requestDto.getNameEn());
        occupation.setNameKh(requestDto.getNameKh());
        occupation.setStatus(requestDto.getStatus());
        repository.save(occupation);
        return toDto(occupation);
    }

    @Override
    public OccupationDto updateOccupation(Long id, OccupationUpdateRequestDto requestDto) {
        Occupation occupation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Occupation not found"));

        // Update English name if provided and check for duplicates
        if (requestDto.getNameEn() != null && !requestDto.getNameEn().equals(occupation.getNameEn())) {
            if (repository.existsByNameEn(requestDto.getNameEn())) {
                throw new DuplicateNameException("Occupation with English name '" + requestDto.getNameEn() + "' already exists");
            }
            occupation.setNameEn(requestDto.getNameEn());
        }

        // Update Khmer name if provided and check for duplicates
        if (requestDto.getNameKh() != null && !requestDto.getNameKh().equals(occupation.getNameKh())) {
            if (repository.existsByNameKh(requestDto.getNameKh())) {
                throw new DuplicateNameException("Occupation with Khmer name '" + requestDto.getNameKh() + "' already exists");
            }
            occupation.setNameKh(requestDto.getNameKh());
        }

        // Update status if provided
        if (requestDto.getStatus() != null) {
            occupation.setStatus(requestDto.getStatus());
        }

        repository.save(occupation);
        return toDto(occupation);
    }


    @Override
    public void deleteOccupation(Long id) {
        repository.deleteById(id);
    }

    private OccupationDto toDto(Occupation occupation) {
        return new OccupationDto(
                occupation.getId(),
                occupation.getNameEn(),
                occupation.getNameKh(),
                occupation.getStatus()
        );
    }
}
