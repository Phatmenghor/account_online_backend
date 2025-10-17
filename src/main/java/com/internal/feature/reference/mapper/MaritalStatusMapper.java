package com.internal.feature.reference.mapper;

import com.internal.enumation.LanguageEnum;
import com.internal.feature.reference.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.reference.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllMaritalStatusResponseDto;
import com.internal.feature.reference.dto.response.MaritalStatusDto;
import com.internal.feature.reference.models.MaritalStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MaritalStatusMapper {

    default MaritalStatusDto toDto(MaritalStatus status, LanguageEnum language) {
        if (status == null) return null;

        MaritalStatusDto dto = new MaritalStatusDto();
        dto.setId(status.getId());
        dto.setStatus(status.getStatus());

        if (LanguageEnum.EN.equals(language)) {
            dto.setNameEn(status.getNameEn());
        } else if (LanguageEnum.KH.equals(language)) {
            dto.setNameKh(status.getNameKh());
        } else {
            dto.setNameEn(status.getNameEn());
            dto.setNameKh(status.getNameKh());
        }

        return dto;
    }

    @Named("mapToListDto")
    default AllMaritalStatusResponseDto mapToListDto(List<MaritalStatusDto> content, Page<MaritalStatus> maritalStatuses) {
        AllMaritalStatusResponseDto maritalStatusesList = new AllMaritalStatusResponseDto();
        maritalStatusesList.setContent(content);
        maritalStatusesList.setPageNo(maritalStatuses.getNumber() + 1);
        maritalStatusesList.setPageSize(maritalStatuses.getSize());
        maritalStatusesList.setTotalElements(maritalStatuses.getTotalElements());
        maritalStatusesList.setTotalPages(maritalStatuses.getTotalPages());
        maritalStatusesList.setLast(maritalStatuses.isLast());
        return maritalStatusesList;
    }

    default MaritalStatus fromCreateDto(MaritalStatusCreateRequestDto request) {
        if (request == null) return null;
        MaritalStatus status = new MaritalStatus();
        status.setNameEn(request.getNameEn());
        status.setNameKh(request.getNameKh());
        status.setStatus(request.getStatus());
        return status;
    }

    default void updateFromDto(MaritalStatusUpdateRequestDto request, @MappingTarget MaritalStatus status) {
        if (request == null || status == null) return;

        if (request.getNameEn() != null) status.setNameEn(request.getNameEn());
        if (request.getNameKh() != null) status.setNameKh(request.getNameKh());
        if (request.getStatus() != null) status.setStatus(request.getStatus());
    }
}
