package com.internal.feature.reference.service;

import com.internal.feature.reference.dto.request.*;
import com.internal.feature.reference.dto.response.AllReferenceDocResponseDto;
import com.internal.feature.reference.dto.response.ReferenceDocDto;

import java.util.List;

public interface ReferenceDocService {
    ReferenceDocDto getById(Long id);
    AllReferenceDocResponseDto getAllReferenceDoc(GetAllReferenceDocRequest request);

    List<ReferenceDocDto> getAllReferenceDocPublic(String search);
    ReferenceDocDto create(ReferenceDocCreateRequestDto request);
    ReferenceDocDto update(Long id, ReferenceDocUpdateRequestDto request);
    ReferenceDocDto delete(Long id);

}
