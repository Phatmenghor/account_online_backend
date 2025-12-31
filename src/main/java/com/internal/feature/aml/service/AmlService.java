package com.internal.feature.aml.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.internal.feature.aml.dto.request.AllAmlHistoryRequestDto;
import com.internal.feature.aml.dto.request.AllAmlRequestDto;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.ExternalAmlStatusUpdateDto;
import com.internal.feature.aml.dto.request.UpdateAmlStatusDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlHistoryDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;

import java.util.Optional;

public interface AmlService {
    Optional<AmlStatus> findByLegalId(String legalId);
    AmlStatus createAmlStatus(CreateAmlRequestDto requestDto) throws JsonProcessingException;
    AmlStatusDto updateAmlStatus(Long id, UpdateAmlStatusDto status) throws JsonProcessingException;
    AllAmlResponseDto getAllAml(AllAmlRequestDto requestDto);
    AmlStatusDto getAmlById(Long id);

    AmlHistoryDto getAmlHistoryById(Long id);

    AllAmlHistoryResponseDto getAllAmlHistory(AllAmlHistoryRequestDto requestDto);

    void updateExternalAmlStatus(ExternalAmlStatusUpdateDto request);
}
