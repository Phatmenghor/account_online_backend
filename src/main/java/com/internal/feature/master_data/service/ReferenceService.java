package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.GetAllReferenceRequest;
import com.internal.feature.master_data.dto.request.ReferenceCreateRequestDto;
import com.internal.feature.master_data.dto.request.ReferenceUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllReferenceResponseDto;
import com.internal.feature.master_data.dto.response.ReferenceDto;

import java.util.List;

public interface ReferenceService {
    ReferenceDto getById(Long id);
    AllReferenceResponseDto getAll(GetAllReferenceRequest request);

    List<ReferenceDto> getAllPublic(String search);
    ReferenceDto create(ReferenceCreateRequestDto request);
    ReferenceDto update(Long id, ReferenceUpdateRequestDto request);
    ReferenceDto delete(Long id);
}
