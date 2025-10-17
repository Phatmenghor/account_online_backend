package com.internal.feature.reference.service;

import com.internal.feature.reference.dto.request.GetAllMaritalStatusRequest;
import com.internal.feature.reference.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.reference.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllMaritalStatusResponseDto;
import com.internal.feature.reference.dto.response.MaritalStatusDto;

public interface MaritalStatusService {
    MaritalStatusDto getById(Long id);
    AllMaritalStatusResponseDto getAll(GetAllMaritalStatusRequest request);
    MaritalStatusDto create(MaritalStatusCreateRequestDto request);
    MaritalStatusDto update(Long id, MaritalStatusUpdateRequestDto request);
    void delete(Long id);
}
