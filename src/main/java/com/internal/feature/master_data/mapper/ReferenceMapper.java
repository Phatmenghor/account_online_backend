package com.internal.feature.master_data.mapper;

import com.internal.feature.master_data.dto.request.ReferenceCreateRequestDto;
import com.internal.feature.master_data.dto.request.ReferenceUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllReferenceResponseDto;
import com.internal.feature.master_data.dto.response.ReferenceDto;
import com.internal.feature.master_data.models.Reference;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReferenceMapper {

    ReferenceMapper INSTANCE = Mappers.getMapper(ReferenceMapper.class);

    /** Entity â†’ DTO */
    ReferenceDto toDto(Reference reference);

    /** Map List of Entities to List of DTOs */
    List<ReferenceDto> toDtoList(List<Reference> references);

    /** Create request â†’ entity */
    Reference fromCreateDto(ReferenceCreateRequestDto request);

    /** Update entity from update request (partial update supported) */
    void updateFromDto(ReferenceUpdateRequestDto request, @MappingTarget Reference reference);

    @Named("mapToListDto")
    default AllReferenceResponseDto mapToListDto(List<ReferenceDto> content, Page<Reference> referencePage) {
        AllReferenceResponseDto response = new AllReferenceResponseDto();
        response.setContent(content);
        response.setPageNo(referencePage.getNumber() + 1);
        response.setPageSize(referencePage.getSize());
        response.setTotalElements(referencePage.getTotalElements());
        response.setTotalPages(referencePage.getTotalPages());
        response.setLast(referencePage.isLast());
        return response;
    }
}
