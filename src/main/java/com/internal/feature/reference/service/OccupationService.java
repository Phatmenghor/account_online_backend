package com.internal.feature.reference.service;

import com.internal.feature.reference.dto.request.GetAllOccupationRequest;
import com.internal.feature.reference.dto.request.OccupationCreateRequestDto;
import com.internal.feature.reference.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllOccupationResponseDto;
import com.internal.feature.reference.dto.response.OccupationDto;

import java.util.List;

public interface OccupationService {
    OccupationDto getOccupationById(Long id);

    OccupationDto getOccupationByCode(String occupationCode);

    AllOccupationResponseDto getAllOccupations(GetAllOccupationRequest request);

    List<OccupationDto> getAllOccupationsPublic(String search);
    OccupationDto createOccupation(OccupationCreateRequestDto requestDto);
    OccupationDto updateOccupation(Long id, OccupationUpdateRequestDto requestDto);
    OccupationDto deleteOccupation(Long id);
}
