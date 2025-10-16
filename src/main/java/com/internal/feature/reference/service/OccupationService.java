package com.internal.feature.reference.service;

import com.internal.feature.reference.dto.request.GetAllOccupationRequest;
import com.internal.feature.reference.dto.request.OccupationCreateRequestDto;
import com.internal.feature.reference.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.reference.dto.response.OccupationDto;

import java.util.List;

public interface OccupationService {
    OccupationDto getOccupationById(Long id);
    List<OccupationDto> getAllOccupations(GetAllOccupationRequest request);
    OccupationDto createOccupation(OccupationCreateRequestDto requestDto);
    OccupationDto updateOccupation(Long id, OccupationUpdateRequestDto requestDto);
    void deleteOccupation(Long id);
}
