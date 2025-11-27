package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.GetAllOccupationRequest;
import com.internal.feature.master_data.dto.request.OccupationCreateRequestDto;
import com.internal.feature.master_data.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllOccupationResponseDto;
import com.internal.feature.master_data.dto.response.OccupationDto;

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
