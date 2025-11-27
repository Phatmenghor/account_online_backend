package com.internal.feature.master_data.mapper;

import com.internal.feature.master_data.dto.request.OccupationCreateRequestDto;
import com.internal.feature.master_data.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllOccupationResponseDto;
import com.internal.feature.master_data.dto.response.OccupationDto;
import com.internal.feature.master_data.models.Occupation;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OccupationMapper {

    OccupationMapper INSTANCE = Mappers.getMapper(OccupationMapper.class);

    /** Entity â†’ DTO */
    OccupationDto toDto(Occupation occupation);

    /** Map List of Entities to List of DTOs */
    List<OccupationDto> toDtoList(List<Occupation> occupations);

    /** Create request â†’ entity */
    Occupation fromCreateDto(OccupationCreateRequestDto request);

    /** Update entity from update request (partial update supported) */
    void updateFromDto(OccupationUpdateRequestDto request, @MappingTarget Occupation occupation);

    @Named("mapToListDto")
    default AllOccupationResponseDto mapToListDto(List<OccupationDto> content, Page<Occupation> occupationPage) {
        AllOccupationResponseDto response = new AllOccupationResponseDto();
        response.setContent(content);
        response.setPageNo(occupationPage.getNumber() + 1);
        response.setPageSize(occupationPage.getSize());
        response.setTotalElements(occupationPage.getTotalElements());
        response.setTotalPages(occupationPage.getTotalPages());
        response.setLast(occupationPage.isLast());
        return response;
    }
}
