package com.internal.feature.reference.service;

import com.internal.feature.reference.dto.request.GetAllReferenceRequest;
import com.internal.feature.reference.dto.request.ReferenceCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllReferenceResponseDto;
import com.internal.feature.reference.dto.response.ReferenceDto;

import java.util.List;

public interface ReferenceService {
    ReferenceDto getById(Long id);
    AllReferenceResponseDto getAll(GetAllReferenceRequest request);

    List<ReferenceDto> getAllPublic(String search);
    ReferenceDto create(ReferenceCreateRequestDto request);
    ReferenceDto update(Long id, ReferenceUpdateRequestDto request);
    ReferenceDto delete(Long id);
}
