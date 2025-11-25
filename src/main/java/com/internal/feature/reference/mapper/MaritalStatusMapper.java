package com.internal.feature.reference.mapper;

import com.internal.feature.reference.dto.request.MaritalStatusCreateRequestDto;
import com.internal.feature.reference.dto.request.MaritalStatusUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllMaritalStatusResponseDto;
import com.internal.feature.reference.dto.response.MaritalStatusDto;
import com.internal.feature.reference.models.MaritalStatus;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MaritalStatusMapper {

    /** Entity → DTO */
    MaritalStatusDto toDto(MaritalStatus maritalStatus);

    /** Map List of Entities to List of DTOs */
    List<MaritalStatusDto> toDtoList(List<MaritalStatus> maritalStatuses);

    /** Create request → entity */
    MaritalStatus fromCreateDto(MaritalStatusCreateRequestDto request);

    /** Update entity from update request (partial update supported) */
    void updateFromDto(MaritalStatusUpdateRequestDto request, @MappingTarget MaritalStatus maritalStatus);

    @Named("mapToListDto")
    default AllMaritalStatusResponseDto mapToListDto(List<MaritalStatusDto> content, Page<MaritalStatus> maritalStatusPage) {
        AllMaritalStatusResponseDto response = new AllMaritalStatusResponseDto();
        response.setContent(content);
        response.setPageNo(maritalStatusPage.getNumber() + 1);
        response.setPageSize(maritalStatusPage.getSize());
        response.setTotalElements(maritalStatusPage.getTotalElements());
        response.setTotalPages(maritalStatusPage.getTotalPages());
        response.setLast(maritalStatusPage.isLast());
        return response;
    }
}
