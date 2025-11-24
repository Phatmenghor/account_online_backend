package com.internal.feature.reference.service;

import com.internal.feature.reference.dto.request.*;
import com.internal.feature.reference.dto.response.AllLegalTypeResponseDto;
import com.internal.feature.reference.dto.response.LegalTypeDto;

import java.util.List;

public interface LegalTypeService {
    LegalTypeDto getById(Long id);
    AllLegalTypeResponseDto getAllLegalType(GetAllLegalTypeRequest request);

    List<LegalTypeDto> getAllLegalTypePublic(String search);
    LegalTypeDto create(LegalTypeCreateRequestDto request);
    LegalTypeDto update(Long id, LegalTypeUpdateRequestDto request);
    LegalTypeDto delete(Long id);

}
