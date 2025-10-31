package com.internal.feature.aml.mapper;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.AmlHistoryRequestDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AmlHistoryDto;
import com.internal.feature.aml.model.AmlHistory;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
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
    @Mapping(target = "changedBy", source = "changedBy")
    @Mapping(target = "idDisplay", source = "idDisplay")
    @Mapping(target = "familyName", source = "familyName")
    @Mapping(target = "givenName", source = "givenName")
    @Mapping(target = "firstNameKh", source = "firstNameKh")
    @Mapping(target = "lastNameKh", source = "lastNameKh")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "nationality", source = "nationality")
    @Mapping(target = "legalAddress", source = "legalAddress")
    @Mapping(target = "oldStatus", source = "oldStatus")
    @Mapping(target = "newStatus", source = "newStatus")
    AmlHistory fromCreateDto(AmlHistoryRequestDto request);

    // -------------------------------
    // ENTITY → DTO
    // -------------------------------
    @Mapping(target = "customerInfo.idDisplay", source = "idDisplay")
    @Mapping(target = "customerInfo.familyName", source = "familyName")
    @Mapping(target = "customerInfo.givenName", source = "givenName")
    @Mapping(target = "customerInfo.firstNameKh", source = "firstNameKh")
    @Mapping(target = "customerInfo.lastNameKh", source = "lastNameKh")
    @Mapping(target = "customerInfo.dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "customerInfo.gender", source = "gender")
    @Mapping(target = "customerInfo.nationality", source = "nationality")
    @Mapping(target = "customerInfo.legalAddress", source = "legalAddress")
    @Mapping(target = "changedBy", source = "changedBy")
    @Mapping(target = "oldStatus", source = "oldStatus")
    @Mapping(target = "newStatus", source = "newStatus")
    AmlHistoryDto toDto(AmlHistory history);

    // -------------------------------
    // HISTORY ENTRY FROM STATUS CHANGE
    // -------------------------------
    default AmlHistory createHistoryFromStatusChange(
            AmlStatus status,
            AmlStatusEnum oldStatus,
            Object changedBy  // MapStruct will ignore if not mapped; fallback to default method
    ) {
        if (status == null) return null;

        AmlHistory history = new AmlHistory();
        history.setOriginalRequest(status.getOriginalRequest());
        history.setOriginalResponse(status.getOriginalResponse());
        history.setOldStatus(oldStatus);
        history.setNewStatus(status.getStatus());

        if (changedBy instanceof com.internal.feature.auth.models.UserEntity) {
            history.setChangedBy((com.internal.feature.auth.models.UserEntity) changedBy);
        }

        // copy customer fields
        history.setIdDisplay(status.getIdDisplay());
        history.setGender(status.getGender());
        history.setFamilyName(status.getFamilyName());
        history.setGivenName(status.getGivenName());
        history.setFirstNameKh(status.getFirstNameKh());
        history.setLastNameKh(status.getLastNameKh());
        history.setDateOfBirth(status.getDateOfBirth());
        history.setNationality(status.getNationality());
        history.setLegalAddress(status.getLegalAddress());

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
