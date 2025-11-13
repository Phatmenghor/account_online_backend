package com.internal.feature.aml.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AmlStatusEnum;
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

    ObjectMapper objectMapper = new ObjectMapper();

    // -------------------------------
    // CREATE DTO → ENTITY
    // -------------------------------
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? request.getStatus() : AmlStatusEnum.PENDING)")
    @Mapping(target = "screeningResult", source = "screeningResult")
    @Mapping(target = "amlExternalRiskLevel", source = "riskLevel")
    @Mapping(target = "amlExternalActionTaken", source = "actionTaken")
    @Mapping(target = "amlExternalServiceName", source = "serviceName")
    @Mapping(target = "amlExternalTotalRulesScore", source = "totalRulesScore")
    @Mapping(target = "amlExternalTrxnID", source = "trxnID")
    @Mapping(target = "amlExternalRulesTriggered", source = "rulesTriggered", qualifiedByName = "objectArrayToJson")
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
        status.setStatus(request.getStatus() != null ? request.getStatus() : AmlStatusEnum.PENDING);
        status.setScreeningResult(request.getScreeningResult());
        status.setAmlExternalRiskLevel(request.getRiskLevel());
        status.setAmlExternalActionTaken(request.getActionTaken());
        status.setAmlExternalServiceName(request.getServiceName());
        status.setAmlExternalTotalRulesScore(request.getTotalRulesScore());
        status.setAmlExternalTrxnID(request.getTrxnID());
        status.setAmlExternalRulesTriggered(objectArrayToJson(request.getRulesTriggered()));

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
    @Mapping(target = "riskLevel", source = "amlExternalRiskLevel")
    @Mapping(target = "actionTaken", source = "amlExternalActionTaken")
    @Mapping(target = "rulesTriggered", source = "amlExternalRulesTriggered", qualifiedByName = "jsonToObjectArray")
    @Mapping(target = "trxnID", source = "amlExternalTrxnID")
    @Mapping(target = "serviceName", source = "amlExternalServiceName")
    @Mapping(target = "totalRulesScore", source = "amlExternalTotalRulesScore")
    AmlStatusDto toStatusDto(AmlStatus status);

    // -------------------------------
    // PAGED RESPONSE
    // -------------------------------
    @Named("mapToListDto")
    default AllAmlResponseDto mapToListDto(List<AmlStatusDto> content, Page<AmlStatus> statuses) {
        AllAmlResponseDto dto = new AllAmlResponseDto();
        dto.setContent(content);
        dto.setPageNo(statuses.getNumber() + 1);
        dto.setPageSize(statuses.getSize());
        dto.setTotalElements(statuses.getTotalElements());
        dto.setTotalPages(statuses.getTotalPages());
        dto.setLast(statuses.isLast());
        return dto;
    }

    // -------------------------------
    // CONVERTERS
    // -------------------------------
    @Named("jsonToObjectArray")
    static Object[] jsonToObjectArray(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, Object[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("❌ Failed to parse JSON string to Object[]: " + json, e);
        }
    }

    @Named("objectArrayToJson")
    static String objectArrayToJson(Object[] array) {
        if (array == null) return null;
        try {
            return objectMapper.writeValueAsString(array);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("❌ Failed to convert Object[] to JSON string", e);
        }
    }
}
