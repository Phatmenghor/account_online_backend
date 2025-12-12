package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.DuplicateNameException;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.GetAllOccupationRequest;
import com.internal.feature.master_data.dto.request.OccupationCreateRequestDto;
import com.internal.feature.master_data.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllOccupationResponseDto;
import com.internal.feature.master_data.dto.response.OccupationDto;
import com.internal.feature.master_data.mapper.OccupationMapper;
import com.internal.feature.master_data.models.Occupation;
import com.internal.feature.master_data.repository.OccupationRepository;
import com.internal.feature.master_data.service.OccupationService;
import com.internal.feature.master_data.specification.OccupationSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OccupationServiceImpl implements OccupationService {

    private final OccupationRepository repository;
    private final OccupationMapper mapper;

    @Override
    public OccupationDto getOccupationById(Long id) {
        Occupation occupation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Occupation not found"));
        return mapper.toDto(occupation);
    }

    @Override
    public OccupationDto getOccupationByCode(String occupationCode) {
        Occupation occupation = repository.findByOccupationCode(occupationCode)
                .orElseThrow(() -> new NotFoundException("Occupation not found"));
        return mapper.toDto(occupation);
    }

    @Override
    public AllOccupationResponseDto getAllOccupations(GetAllOccupationRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        // Build specification dynamically
        Specification<Occupation> spec = OccupationSpec.hasStatus(request.getStatus())
                .and(OccupationSpec.searchByName(request.getSearch()));

        Page<Occupation> page = repository.findAll(spec, pageable);

        List<OccupationDto> content = page.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return mapper.mapToListDto(content, page);
    }

    @Override
    public List<OccupationDto> getAllOccupationsPublic(String search) {
        // Build specification dynamically - FORCE ACTIVE STATUS
        Specification<Occupation> spec = OccupationSpec.hasStatus(com.internal.enumation.StatusData.ACTIVE)
                .and(OccupationSpec.searchByName(search));

        List<Occupation> occupations = repository.findAll(spec);
        return mapper.toDtoList(occupations);
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
        return mapper.toDto(occupation);
    }

    @Override
    public OccupationDto updateOccupation(Long id, OccupationUpdateRequestDto requestDto) {
        Occupation occupation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Occupation not found"));

        mapper.updateFromDto(requestDto, occupation);

        repository.save(occupation);
        return mapper.toDto(occupation);
    }

    @Override
    public OccupationDto deleteOccupation(Long id) {
        log.info("Deleting occupation with id: {}", id);

        Occupation occupation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " could not be found"));

        repository.deleteById(id);
        return mapper.toDto(occupation);
    }
}
