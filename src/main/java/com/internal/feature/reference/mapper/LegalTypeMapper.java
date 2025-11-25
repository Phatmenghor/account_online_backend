package com.internal.feature.reference.mapper;

import com.internal.feature.reference.dto.request.LegalTypeCreateRequestDto;
import com.internal.feature.reference.dto.request.LegalTypeUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllLegalTypeResponseDto;
import com.internal.feature.reference.dto.response.LegalTypeDto;
import com.internal.feature.reference.models.LegalType;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LegalTypeMapper {

    /** Entity → DTO */
    LegalTypeDto toDto(LegalType legalType);

    /** Map List of Entities to List of DTOs */
    List<LegalTypeDto> toDtoList(List<LegalType> legalTypes);

    /** Create request → entity */
    LegalType fromCreateDto(LegalTypeCreateRequestDto request);

    /** Update entity from update request (partial update supported) */
    void updateFromDto(LegalTypeUpdateRequestDto request, @MappingTarget LegalType legalType);

    @Named("mapToListDto")
    default AllLegalTypeResponseDto mapToListDto(List<LegalTypeDto> content, Page<LegalType> legalTypePage) {
        AllLegalTypeResponseDto response = new AllLegalTypeResponseDto();
        response.setContent(content);
        response.setPageNo(legalTypePage.getNumber() + 1);
        response.setPageSize(legalTypePage.getSize());
        response.setTotalElements(legalTypePage.getTotalElements());
        response.setTotalPages(legalTypePage.getTotalPages());
        response.setLast(legalTypePage.isLast());
        return response;
    }
}
