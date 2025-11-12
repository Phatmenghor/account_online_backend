package com.internal.feature.aml.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.internal.feature.aml.dto.request.AmlHistoryRequestDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AmlHistoryDto;
import com.internal.feature.aml.model.AmlHistory;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.auth.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AmlHistoryMapper {

    // -------------------------------
    // CREATE DTO → ENTITY
    // -------------------------------
    @Mapping(target = "approvedBy", source = "changedBy")
    @Mapping(target = "currentAddressName", source = "legalAddress")
    AmlHistory fromCreateDto(AmlHistoryRequestDto request);

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
    @Mapping(target = "changedBy", source = "approvedBy")
    AmlHistoryDto toDto(AmlHistory history);

    // -------------------------------
    // HISTORY ENTRY FROM STATUS CHANGE
    // -------------------------------
    default AmlHistory createHistoryFromStatusChange(
            AmlStatus status,
            Object changedBy
    ) throws JsonProcessingException {
        if (status == null) return null;

        AmlHistory history = new AmlHistory();
        history.setScreeningResult(status.getScreeningResult());
        history.setStatus(status.getStatus());

        if (changedBy instanceof UserEntity) {
            history.setApprovedBy((UserEntity) changedBy);
        }

        // copy customer fields
        history.setLegalId(status.getLegalId());
        history.setGender(status.getGender());
        history.setFamilyName(status.getFamilyName());
        history.setGivenName(status.getGivenName());
        history.setFirstNameKh(status.getFirstNameKh());
        history.setLastNameKh(status.getLastNameKh());
        history.setDateOfBirth(status.getDateOfBirth());
        history.setNationality(status.getNationality());
        history.setCurrentAddressName(status.getCurrentAddressName());

        // =============================
        // AML middleware - external service fields
        // =============================
        history.setAmlExternalActionTaken(status.getAmlExternalActionTaken());
        history.setAmlExternalRiskLevel(status.getAmlExternalRiskLevel());
        history.setAmlExternalTrxnID(status.getAmlExternalTrxnID());
        history.setAmlExternalTotalRulesScore(status.getAmlExternalTotalRulesScore());
        history.setAmlExternalServiceName(status.getAmlExternalServiceName());

        // Convert rules triggered array to JSON string if needed
        if (status.getAmlExternalRulesTriggered() != null) {
            history.setAmlExternalRulesTriggered(
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(status.getAmlExternalRulesTriggered())
            );
        }

        return history;
    }


    // -------------------------------
    // PAGED RESPONSE
    // -------------------------------
    @Named("mapToListDto")
    default AllAmlHistoryResponseDto mapToListDto(List<AmlHistoryDto> content, Page<AmlHistory> histories) {
        AllAmlHistoryResponseDto response = new AllAmlHistoryResponseDto();
        response.setContent(content);
        response.setPageNo(histories.getNumber() + 1);
        response.setPageSize(histories.getSize());
        response.setTotalElements(histories.getTotalElements());
        response.setTotalPages(histories.getTotalPages());
        response.setLast(histories.isLast());
        return response;
    }
}
