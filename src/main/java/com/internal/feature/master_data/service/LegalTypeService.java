package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.*;
import com.internal.feature.master_data.dto.response.AllLegalTypeResponseDto;
import com.internal.feature.master_data.dto.response.LegalTypeDto;

import java.util.List;

public interface LegalTypeService {
    LegalTypeDto getById(Long id);
    AllLegalTypeResponseDto getAllLegalType(GetAllLegalTypeRequest request);

    List<LegalTypeDto> getAllLegalTypePublic(String search);
    LegalTypeDto create(LegalTypeCreateRequestDto request);
    LegalTypeDto update(Long id, LegalTypeUpdateRequestDto request);
    LegalTypeDto delete(Long id);

}
