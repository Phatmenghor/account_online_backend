package com.internal.feature.aml.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AmlStatusMapper {

    // -------------------------------
    // CREATE DTO → ENTITY
    // -------------------------------
    @Mapping(target = "legalId", source = "legalId")
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? request.getStatus() : com.internal.enumation.AmlStatusEnum.PENDING)")
    AmlStatus fromCreateDto(CreateAmlRequestDto request);

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

    // -------------------------------
    // CUSTOMER REQUEST + RESPONSE → CREATE_AML_DTO
    // -------------------------------
    default CreateAmlRequestDto toCreateAmlRequestDto(CustomerRequest request, Map<String, Object> responseMap) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String requestJson = objectMapper.writeValueAsString(request);
            String responseJson = objectMapper.writeValueAsString(responseMap);

            return CreateAmlRequestDto.builder()
                    .originalRequest(requestJson)
                    .originalResponse(responseJson)
                    .status(AmlStatusEnum.PENDING)
                    .legalId(request.getLegalId())
                    .familyName(request.getFamilyName())
                    .givenName(request.getGivenName())
                    .firstNameKh(request.getFirstNameKh())
                    .lastNameKh(request.getLastNameKh())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .nationality(request.getNationality())
                    .legalAddress(request.getLegalAddress())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to map CustomerRequest to CreateAmlRequestDto", e);
        }
    }
}
