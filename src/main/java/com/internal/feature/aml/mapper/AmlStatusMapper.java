package com.internal.feature.aml.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AmlStatusMapper {

    ObjectMapper objectMapper = new ObjectMapper();

    // ============================================================
    // CREATE DTO → ENTITY
    // ============================================================
    @Mappings({
            @Mapping(target = "approvedBy", source = "approvedBy"),
            @Mapping(target = "rejectedBy", source = "rejectedBy"),
            @Mapping(target = "status",
                    expression = "java(request.getStatus() != null ? request.getStatus() : com.internal.enumation.AmlStatusEnum.PENDING)"),
            @Mapping(target = "amlExternalRiskLevel", source = "riskLevel"),
            @Mapping(target = "amlExternalActionTaken", source = "actionTaken"),
            @Mapping(target = "amlExternalServiceName", source = "serviceName"),
            @Mapping(target = "amlExternalTotalRulesScore", source = "totalRulesScore"),
            @Mapping(target = "amlExternalTrxnID", source = "trxnID"),
            @Mapping(target = "amlExternalRulesTriggered", source = "rulesTriggered"),
            // Only map codes; names will be set in service
            @Mapping(target = "currentAddressCode",
                    expression = "java(request.getCustomerCurrentProvince() + \"-\" + request.getCustomerCurrentDistrict() + \"-\" + request.getCustomerCurrentCommune() + \"-\" + request.getCustomerCurrentVillage())"),
            @Mapping(target = "placeOfBirthCode",
                    expression = "java(request.getCustomerPobProvince() + \"-\" + request.getCustomerPobDistrict() + \"-\" + request.getCustomerPobCommune() + \"-\" + request.getCustomerPobVillage())"),
            // Personal info
            @Mapping(target = "legalId", source = "legalId"),
            @Mapping(target = "familyName", source = "familyName"),
            @Mapping(target = "givenName", source = "givenName"),
            @Mapping(target = "firstNameKh", source = "firstNameKh"),
            @Mapping(target = "lastNameKh", source = "lastNameKh"),
            @Mapping(target = "dateOfBirth", source = "dateOfBirth"),
            @Mapping(target = "gender", source = "gender"),
            @Mapping(target = "nationality", source = "nationality"),
            @Mapping(target = "phoneNumber", source = "phoneNumber"),
            @Mapping(target = "maritalStatus", source = "maritalStatus"),
            @Mapping(target = "occupationCode", source = "occupationCode"),
            @Mapping(target = "occupationStatus", source = "occupationStatus"),
            // Document
            @Mapping(target = "issuedDate", source = "issuedDate"),
            @Mapping(target = "expiredDate", source = "expiredDate")
    })
    AmlStatus fromCreateDto(CreateAmlRequestDto request);

    // ============================================================
    // ENTITY → DTO
    // ============================================================
    @Mappings({
            // Customer
            @Mapping(target = "legalId", source = "legalId"),
            @Mapping(target = "familyName", source = "familyName"),
            @Mapping(target = "givenName", source = "givenName"),
            @Mapping(target = "firstNameKh", source = "firstNameKh"),
            @Mapping(target = "lastNameKh", source = "lastNameKh"),
            @Mapping(target = "dateOfBirth", source = "dateOfBirth"),
            @Mapping(target = "gender", source = "gender"),
            @Mapping(target = "nationality", source = "nationality"),
            @Mapping(target = "phoneNumber", source = "phoneNumber"),
            @Mapping(target = "maritalStatus", source = "maritalStatus"),
            @Mapping(target = "occupationCode", source = "occupationCode"),
            @Mapping(target = "occupationStatus", source = "occupationStatus"),

            // Document
            @Mapping(target = "issuedDate", source = "issuedDate"),
            @Mapping(target = "expiredDate", source = "expiredDate"),

            // Address
            @Mapping(target = "legalAddress", source = "currentAddressName"),
            @Mapping(target = "currentAddressName", source = "currentAddressName"),
            @Mapping(target = "currentAddressCode", source = "currentAddressCode"),
            @Mapping(target = "placeOfBirthName", source = "placeOfBirthName"),
            @Mapping(target = "placeOfBirthCode", source = "placeOfBirthCode"),

            // AML
            @Mapping(target = "riskLevel", source = "amlExternalRiskLevel"),
            @Mapping(target = "actionTaken", source = "amlExternalActionTaken"),
            @Mapping(target = "rulesTriggered", source = "amlExternalRulesTriggered", qualifiedByName = "jsonToObjectArray"),
            @Mapping(target = "serviceName", source = "amlExternalServiceName"),
            @Mapping(target = "totalRulesScore", source = "amlExternalTotalRulesScore"),
            @Mapping(target = "trxnID", source = "amlExternalTrxnID"),

            // Remarks
            @Mapping(target = "remarks", source = "remarks"),

            // Users
            @Mapping(target = "approvedBy", source = "approvedBy"),
            @Mapping(target = "rejectedBy", source = "rejectedBy")
    })
    AmlStatusDto toStatusDto(AmlStatus status);

    // ============================================================
    // PAGINATION MAPPING
    // ============================================================
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

    // ============================================================
    // JSON HELPERS
    // ============================================================
    @Named("jsonToObjectArray")
    static Object[] jsonToObjectArray(String json) {
        try { return json == null ? null : objectMapper.readValue(json, Object[].class); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Named("objectArrayToJson")
    static String objectArrayToJson(Object[] array) {
        try { return array == null ? null : objectMapper.writeValueAsString(array); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
