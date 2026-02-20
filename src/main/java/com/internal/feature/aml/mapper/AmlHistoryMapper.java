package com.internal.feature.aml.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AmlStatusEnum;
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

    ObjectMapper objectMapper = new ObjectMapper();

        // -------------------------------
        // CREATE DTO â†’ ENTITY
        // -------------------------------
        @Mapping(target = "approvedBy", source = "approvedBy")
        @Mapping(target = "rejectedBy", source = "rejectedBy")
        @Mapping(target = "legalId", source = "legalId")
        @Mapping(target = "familyName", source = "familyName")
        @Mapping(target = "givenName", source = "givenName")
        @Mapping(target = "firstNameKh", source = "firstNameKh")
        @Mapping(target = "lastNameKh", source = "lastNameKh")
        @Mapping(target = "dateOfBirth", source = "dateOfBirth")
        @Mapping(target = "gender", source = "gender")
        @Mapping(target = "nationality", source = "nationality")
        @Mapping(target = "phoneNumber", source = "phoneNumber")
        @Mapping(target = "maritalStatus", source = "maritalStatus")
        @Mapping(target = "issuedDate", source = "issuedDate")
        @Mapping(target = "expiredDate", source = "expiredDate")
        @Mapping(target = "currentAddressName", source = "legalAddress")
        @Mapping(target = "occupationCode", source = "occupationCode")
        @Mapping(target = "occupationStatus", source = "occupationStatus")
        @Mapping(target = "screeningResult", source = "screeningResult")
        @Mapping(target = "amlExternalRiskLevel", source = "riskLevel")
        @Mapping(target = "amlExternalActionTaken", source = "actionTaken")
        @Mapping(target = "amlExternalRulesTriggered", source = "rulesTriggered")
        @Mapping(target = "amlExternalServiceName", source = "serviceName")
        @Mapping(target = "amlExternalTotalRulesScore", source = "totalRulesScore")
        @Mapping(target = "amlExternalTrxnID", source = "trxnID")
        @Mapping(target = "nidImageName", source = "nidImageName")
        @Mapping(target = "selfieImageName", source = "selfieImageName")
        AmlHistory fromCreateDto(AmlHistoryRequestDto request);

    // -------------------------------
    // ENTITY â†’ DTO
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
    @Mapping(target = "customerInfo.phoneNumber", source = "phoneNumber")
    @Mapping(target = "customerInfo.issuedDate", source = "issuedDate")
    @Mapping(target = "customerInfo.expiredDate", source = "expiredDate")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "rulesTriggered", source = "amlExternalRulesTriggered")
    @Mapping(target = "riskLevel", source = "amlExternalRiskLevel")
    @Mapping(target = "trxnID", source = "amlExternalTrxnID")
    @Mapping(target = "serviceName", source = "amlExternalServiceName")
    @Mapping(target = "actionTaken", source = "amlExternalActionTaken")
    @Mapping(target = "totalRulesScore", source = "amlExternalTotalRulesScore")
    @Mapping(target = "nidImageName", source = "nidImageName")
    @Mapping(target = "selfieImageName", source = "selfieImageName")
    AmlHistoryDto toDto(AmlHistory history);

    // -------------------------------
    // HISTORY ENTRY FROM STATUS CHANGE
    // -------------------------------
    default AmlHistory createHistoryFromStatusChange(AmlStatus status, Object changedBy) {
        if (status == null) return null;

        AmlHistory history = new AmlHistory();
        history.setScreeningResult(status.getScreeningResult());
        history.setStatus(status.getStatus());

        if (changedBy instanceof UserEntity) {
            UserEntity user = (UserEntity) changedBy;

            if (status.getStatus() == AmlStatusEnum.APPROVE) {
                history.setApprovedBy(user);
                history.setRejectedBy(null);
            } else if (status.getStatus() == AmlStatusEnum.REJECT) {
                history.setRejectedBy(user);
                history.setApprovedBy(null);
            } else {
                history.setApprovedBy(null);
                history.setRejectedBy(null);
            }
        }


        // Copy customer details
        history.setLegalId(status.getLegalId());
        history.setGender(status.getGender());
        history.setFamilyName(status.getFamilyName());
        history.setGivenName(status.getGivenName());
        history.setFirstNameKh(status.getFirstNameKh());
        history.setLastNameKh(status.getLastNameKh());
        history.setDateOfBirth(status.getDateOfBirth());
        history.setNationality(status.getNationality());
        history.setCurrentAddressName(status.getCurrentAddressName());
        history.setCurrentAddressCode(status.getCurrentAddressCode());
        history.setPlaceOfBirthName(status.getPlaceOfBirthName());
        history.setPlaceOfBirthCode(status.getPlaceOfBirthCode());
        history.setIssuedDate(status.getIssuedDate());
        history.setExpiredDate(status.getExpiredDate());
        history.setPlaceOfBirthName(status.getPlaceOfBirthName());

        // Copy AML middleware fields
        history.setAmlExternalActionTaken(status.getAmlExternalActionTaken());
        history.setAmlExternalRiskLevel(status.getAmlExternalRiskLevel());
        history.setAmlExternalTrxnID(status.getAmlExternalTrxnID());
        history.setAmlExternalTotalRulesScore(status.getAmlExternalTotalRulesScore());
        history.setAmlExternalServiceName(status.getAmlExternalServiceName());
        history.setExpiredDate(status.getExpiredDate());
        history.setIssuedDate(status.getIssuedDate());
        history.setMaritalStatus(status.getMaritalStatus());
        history.setPhoneNumber(status.getPhoneNumber());
        history.setOccupationStatus(status.getOccupationStatus());
        history.setOccupationCode(status.getOccupationCode());

        // Convert rulesTriggered safely
        history.setAmlExternalRulesTriggered(status.getAmlExternalRulesTriggered());

        // Image filenames
        history.setNidImageName(status.getNidImageName());
        history.setSelfieImageName(status.getSelfieImageName());

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
