package com.internal.feature.aml.mapper;

import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AmlStatusMapper {

    // -------------------------------
    // CREATE DTO → ENTITY
    // -------------------------------
    @Mapping(target = "legalId", source = "legalId")
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? request.getStatus() : com.internal.enumation.AmlStatusEnum.PENDING)")
    @Mapping(target = "screeningResult", source = "screeningResult")
    @Mapping(target = "amlExternalRiskLevel", source = "RiskLevel")
    @Mapping(target = "amlExternalActionTaken", source = "ActionTaken")
    @Mapping(target = "amlExternalServiceName", source = "ServiceName")
    @Mapping(target = "amlExternalTotalRulesScore", source = "TotalRulesScore")
    @Mapping(target = "amlExternalTrxnID", source = "TrxnID")
    default AmlStatus fromCreateDto(CreateAmlRequestDto request) {
        if (request == null) return null;

        AmlStatus status = new AmlStatus();
        status.setLegalId(request.getLegalId());
        status.setFamilyName(request.getFamilyName());
        status.setGivenName(request.getGivenName());
        status.setFirstNameKh(request.getFirstNameKh());
        status.setLastNameKh(request.getLastNameKh());
        status.setDateOfBirth(request.getDateOfBirth());
        status.setGender(request.getGender());
        status.setNationality(request.getNationality());
        status.setCurrentAddressName(request.getLegalAddress());
        status.setStatus(request.getStatus() != null ? request.getStatus() : com.internal.enumation.AmlStatusEnum.PENDING);
        status.setScreeningResult(request.getScreeningResult());
        status.setAmlExternalRiskLevel(request.getRiskLevel());
        status.setAmlExternalActionTaken(request.getActionTaken());
        status.setAmlExternalServiceName(request.getServiceName());
        status.setAmlExternalTotalRulesScore(request.getTotalRulesScore());
        status.setAmlExternalTrxnID(request.getTrxnID());

        // Convert RulesTriggered to JSON string
        if (request.getRulesTriggered() != null) {
            try {
                status.setAmlExternalRulesTriggered(
                        new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.getRulesTriggered())
                );
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                status.setAmlExternalRulesTriggered("[]"); // fallback
            }
        }

        return status;
    }

    // -------------------------------
    // ENTITY → DTO
    // -------------------------------
    @Mapping(target = "customerInfo.legalId", source = "legalId")
    @Mapping(target = "customerInfo.familyName", source = "familyName")
    @Mapping(target = "customerInfo.givenName", source = "givenName")
    @Mapping(target = "customerInfo.firstNameKh", source = "firstNameKh")
    @Mapping(target = "customerInfo.lastNameKh", source = "lastNameKh")
    @Mapping(target = "customerInfo.dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "customerInfo.gender", source = "gender")
    @Mapping(target = "customerInfo.nationality", source = "nationality")
    @Mapping(target = "customerInfo.legalAddress", source = "currentAddressName")
    AmlStatusDto toStatusDto(AmlStatus status);

    // -------------------------------
    // PAGED RESPONSE
    // -------------------------------
    @Named("mapToListDto")
    default AllAmlResponseDto mapToListDto(List<AmlStatusDto> content, Page<AmlStatus> statuses) {
        AllAmlResponseDto amlDtoList = new AllAmlResponseDto();
        amlDtoList.setContent(content);
        amlDtoList.setPageNo(statuses.getNumber() + 1);
        amlDtoList.setPageSize(statuses.getSize());
        amlDtoList.setTotalElements(statuses.getTotalElements());
        amlDtoList.setTotalPages(statuses.getTotalPages());
        amlDtoList.setLast(statuses.isLast());
        return amlDtoList;
    }
}
