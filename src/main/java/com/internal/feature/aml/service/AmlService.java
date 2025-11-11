package com.internal.feature.aml.service;

import com.internal.feature.aml.dto.request.AllAmlHistoryRequestDto;
import com.internal.feature.aml.dto.request.AllAmlRequestDto;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.UpdateAmlStatusDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;

import java.util.Optional;

public interface AmlService {
    Optional<AmlStatus> findByLegalId(String legalId);
    AmlStatusDto createAmlStatus(CreateAmlRequestDto requestDto);
    AmlStatusDto updateAmlStatus(Long id, UpdateAmlStatusDto status);
    AllAmlResponseDto getAllAml(AllAmlRequestDto requestDto);
    AllAmlHistoryResponseDto getAllAmlHistory(AllAmlHistoryRequestDto requestDto);
}
