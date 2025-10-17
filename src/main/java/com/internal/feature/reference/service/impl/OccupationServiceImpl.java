package com.internal.feature.reference.service.impl;

import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.reference.dto.request.GetAllOccupationRequest;
import com.internal.feature.reference.dto.request.OccupationCreateRequestDto;
import com.internal.feature.reference.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllOccupationResponseDto;
import com.internal.feature.reference.dto.response.OccupationDto;
import com.internal.feature.reference.mapper.OccupationMapper;
import com.internal.feature.reference.models.Occupation;
import com.internal.feature.reference.repository.OccupationRepository;
import com.internal.feature.reference.service.OccupationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OccupationServiceImpl implements OccupationService {

    private final OccupationRepository repository;
    private final OccupationMapper mapper;

    @Override
    public OccupationDto getOccupationById(Long id) {
        Occupation occupation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Occupation not found"));
        return mapper.toDto(occupation, null);
    }

    @Override
    public AllOccupationResponseDto getAllOccupations(GetAllOccupationRequest request) {
        // Create Pageable
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        // Fetch paginated data
        Page<Occupation> page = repository.findAll(pageable);

        // Apply filters and map to DTOs
        List<OccupationDto> content = page.stream()
                .filter(occ -> request.getStatus() == null || occ.getStatus() == request.getStatus())
                .filter(occ -> request.getSearch() == null ||
                        occ.getNameEn().toLowerCase().contains(request.getSearch().toLowerCase()) ||
                        occ.getNameKh().toLowerCase().contains(request.getSearch().toLowerCase()))
                .map(occ -> mapper.toDto(occ, request.getLanguage()))
                .collect(Collectors.toList());

        // Map to response DTO with pagination metadata
        return mapper.mapToListDto(content, page);
    }

    @Override
    public OccupationDto createOccupation(OccupationCreateRequestDto requestDto) {
        if (repository.existsByNameEn(requestDto.getNameEn())) {
            throw new DuplicateNameException("Occupation with English name '" + requestDto.getNameEn() + "' already exists");
        }
        if (repository.existsByNameKh(requestDto.getNameKh())) {
            throw new DuplicateNameException("Occupation with Khmer name '" + requestDto.getNameKh() + "' already exists");
        }

        Occupation occupation = mapper.fromCreateDto(requestDto);
        repository.save(occupation);
        return mapper.toDto(occupation, null);
    }

    @Override
    public OccupationDto updateOccupation(Long id, OccupationUpdateRequestDto requestDto) {
        Occupation occupation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Occupation not found"));

        mapper.updateFromDto(requestDto, occupation);

        repository.save(occupation);
        return mapper.toDto(occupation, null);
    }

    @Override
    public void deleteOccupation(Long id) {
        repository.deleteById(id);
    }
}
