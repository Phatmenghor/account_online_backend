package com.internal.feature.reference.service;

import com.internal.feature.reference.dto.request.GetAllOccupationRequest;
import com.internal.feature.reference.dto.request.OccupationCreateRequestDto;
import com.internal.feature.reference.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllOccupationResponseDto;
import com.internal.feature.reference.dto.response.OccupationDto;

public interface OccupationService {
    OccupationDto getOccupationById(Long id);
    AllOccupationResponseDto getAllOccupations(GetAllOccupationRequest request);
    OccupationDto createOccupation(OccupationCreateRequestDto requestDto);
    OccupationDto updateOccupation(Long id, OccupationUpdateRequestDto requestDto);
    void deleteOccupation(Long id);
}
