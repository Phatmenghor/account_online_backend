package com.internal.feature.aml.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AmlStatusMapper {

    ObjectMapper objectMapper = new ObjectMapper();

    // ============================================================
    // CREATE DTO â†’ ENTITY
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
            @Mapping(target = "expiredDate", source = "expiredDate"),
            // Image filenames
            @Mapping(target = "nidImageName", source = "nidImageName"),
            @Mapping(target = "selfieImageName", source = "selfieImageName")
    })
    AmlStatus fromCreateDto(CreateAmlRequestDto request);

    // ============================================================
    // UPDATE EXISTING ENTITY FROM CREATE DTO (UPSERT)
    // ============================================================
    @Mappings({
            @Mapping(target = "status", expression = "java(com.internal.enumation.AmlStatusEnum.PENDING)"),
            @Mapping(target = "approvedBy", ignore = true),
            @Mapping(target = "rejectedBy", ignore = true),
            @Mapping(target = "amlExternalRiskLevel", source = "riskLevel"),
            @Mapping(target = "amlExternalActionTaken", source = "actionTaken"),
            @Mapping(target = "amlExternalServiceName", source = "serviceName"),
            @Mapping(target = "amlExternalTotalRulesScore", source = "totalRulesScore"),
            @Mapping(target = "amlExternalTrxnID", source = "trxnID"),
            @Mapping(target = "amlExternalRulesTriggered", source = "rulesTriggered"),
            @Mapping(target = "currentAddressCode",
                    expression = "java(request.getCustomerCurrentProvince() + \"-\" + request.getCustomerCurrentDistrict() + \"-\" + request.getCustomerCurrentCommune() + \"-\" + request.getCustomerCurrentVillage())"),
            @Mapping(target = "placeOfBirthCode",
                    expression = "java(request.getCustomerPobProvince() + \"-\" + request.getCustomerPobDistrict() + \"-\" + request.getCustomerPobCommune() + \"-\" + request.getCustomerPobVillage())"),
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
            @Mapping(target = "issuedDate", source = "issuedDate"),
            @Mapping(target = "expiredDate", source = "expiredDate"),
            @Mapping(target = "nidImageName", source = "nidImageName"),
            @Mapping(target = "selfieImageName", source = "selfieImageName"),
            @Mapping(target = "remarks", ignore = true),
            @Mapping(target = "screeningResult", ignore = true),
            @Mapping(target = "currentAddressName", ignore = true),
            @Mapping(target = "placeOfBirthName", ignore = true)
    })
    void updateFromCreateDto(CreateAmlRequestDto request, @MappingTarget AmlStatus status);

    // ============================================================
    // ENTITY â†’ DTO (NO DUPLICATE NESTED MAPPING)
    // ============================================================
    @Mappings({
            @Mapping(target = "customerInfo.legalId",      source = "legalId"),
            @Mapping(target = "customerInfo.familyName",   source = "familyName"),
            @Mapping(target = "customerInfo.givenName",    source = "givenName"),
            @Mapping(target = "customerInfo.firstNameKh",  source = "firstNameKh"),
            @Mapping(target = "customerInfo.lastNameKh",   source = "lastNameKh"),
            @Mapping(target = "customerInfo.dateOfBirth",  source = "dateOfBirth"),
            @Mapping(target = "customerInfo.gender",       source = "gender"),
            @Mapping(target = "customerInfo.nationality",  source = "nationality"),
            @Mapping(target = "customerInfo.phoneNumber",  source = "phoneNumber"),
            @Mapping(target = "customerInfo.issuedDate", source = "issuedDate"),
            @Mapping(target = "customerInfo.expiredDate", source = "expiredDate"),

            // Address to customerInfo
            @Mapping(target = "customerInfo.legalAddress", source = "currentAddressName"),


            @Mapping(target = "placeOfBirthName", source = "placeOfBirthName"),

            // AML external info
            @Mapping(target = "riskLevel",       source = "amlExternalRiskLevel"),
            @Mapping(target = "actionTaken",     source = "amlExternalActionTaken"),
            @Mapping(target = "rulesTriggered",  source = "amlExternalRulesTriggered"),
            @Mapping(target = "serviceName",     source = "amlExternalServiceName"),
            @Mapping(target = "totalRulesScore", source = "amlExternalTotalRulesScore"),
            @Mapping(target = "trxnID",          source = "amlExternalTrxnID"),

            // Users
            @Mapping(target = "approvedBy", source = "approvedBy"),
            @Mapping(target = "rejectedBy", source = "rejectedBy"),
            // Image filenames
            @Mapping(target = "nidImageName", source = "nidImageName"),
            @Mapping(target = "selfieImageName", source = "selfieImageName"),
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
