package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.GetAllMaritalStatusRequest;
import com.internal.feature.master_data.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.master_data.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllMaritalStatusResponseDto;
import com.internal.feature.master_data.dto.response.MaritalStatusDto;

import java.util.List;

public interface MaritalStatusService {
    MaritalStatusDto getById(Long id);
    AllMaritalStatusResponseDto getAll(GetAllMaritalStatusRequest request);

    List<MaritalStatusDto> getAllPublic(String search);
    MaritalStatusDto create(MaritalStatusCreateRequestDto request);
    MaritalStatusDto update(Long id, MaritalStatusUpdateRequestDto request);
    MaritalStatusDto delete(Long id);
}
