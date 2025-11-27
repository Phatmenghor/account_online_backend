package com.internal.feature.master_data.mapper;

import com.internal.feature.master_data.dto.request.LegalTypeCreateRequestDto;
import com.internal.feature.master_data.dto.request.LegalTypeUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllLegalTypeResponseDto;
import com.internal.feature.master_data.dto.response.LegalTypeDto;
import com.internal.feature.master_data.models.LegalType;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LegalTypeMapper {

    /** Entity â†’ DTO */
    LegalTypeDto toDto(LegalType legalType);

    /** Map List of Entities to List of DTOs */
    List<LegalTypeDto> toDtoList(List<LegalType> legalTypes);

    /** Create request â†’ entity */
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
