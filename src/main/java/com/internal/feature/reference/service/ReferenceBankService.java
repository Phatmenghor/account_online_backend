package com.internal.feature.reference.service;

import com.internal.feature.reference.dto.request.GetAllReferenceBankRequest;
import com.internal.feature.reference.dto.request.ReferenceBankCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceBankUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllReferenceBankResponseDto;
import com.internal.feature.reference.dto.response.ReferenceBankDto;

public interface ReferenceBankService {
    ReferenceBankDto getById(Long id);
    AllReferenceBankResponseDto getAll(GetAllReferenceBankRequest request);
    ReferenceBankDto create(ReferenceBankCreateRequestDto request);
    ReferenceBankDto update(Long id, ReferenceBankUpdateRequestDto request);
    void delete(Long id);
}
