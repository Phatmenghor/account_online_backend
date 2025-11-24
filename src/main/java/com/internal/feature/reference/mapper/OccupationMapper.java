package com.internal.feature.reference.mapper;

import com.internal.feature.reference.dto.request.OccupationCreateRequestDto;
import com.internal.feature.reference.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllOccupationResponseDto;
import com.internal.feature.reference.dto.response.OccupationDto;
import com.internal.feature.reference.models.Occupation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OccupationMapper {

    OccupationMapper INSTANCE = Mappers.getMapper(OccupationMapper.class);

    /** Map Occupation entity to DTO with optional language filtering */
    default OccupationDto toDto(Occupation occupation) {
        if (occupation == null) return null;

        OccupationDto dto = new OccupationDto();
        dto.setId(occupation.getId());
        dto.setStatus(occupation.getStatus());
            dto.setNameEn(occupation.getNameEn());
            dto.setNameKh(occupation.getNameKh());
            dto.setOccupationCode(occupation.getOccupationCode());
        return dto;
    }

    /** Map List of Entities to List of DTOs */
    List<OccupationDto> toDtoList(List<Occupation> occupations);

    @Named("mapToListDto")
    default AllOccupationResponseDto mapToListDto(List<OccupationDto> content, Page<Occupation> occupations) {
        AllOccupationResponseDto occupationResponseDto = new AllOccupationResponseDto();
        occupationResponseDto.setContent(content);
        occupationResponseDto.setPageNo(occupations.getNumber() + 1);
        occupationResponseDto.setPageSize(occupations.getSize());
        occupationResponseDto.setTotalElements(occupations.getTotalElements());
        occupationResponseDto.setTotalPages(occupations.getTotalPages());
        occupationResponseDto.setLast(occupations.isLast());
        return occupationResponseDto;
    }

    /** Map create request to entity */
    default Occupation fromCreateDto(OccupationCreateRequestDto request) {
        if (request == null) return null;
        Occupation occupation = new Occupation();
        occupation.setNameEn(request.getNameEn());
        occupation.setNameKh(request.getNameKh());
        occupation.setStatus(request.getStatus());
        occupation.setOccupationCode(request.getOccupationCode());
        return occupation;
    }

    /** Update existing entity with update request */
    default void updateFromDto(OccupationUpdateRequestDto request, @MappingTarget Occupation occupation) {
        if (request == null || occupation == null) return;

        if (request.getNameEn() != null) {
            occupation.setNameEn(request.getNameEn());
        }

        if (request.getNameKh() != null) {
            occupation.setNameKh(request.getNameKh());
        }

        if (request.getOccupationCode() != null) {
            occupation.setOccupationCode(request.getOccupationCode());
        }

        if (request.getStatus() != null) {
            occupation.setStatus(request.getStatus());
        }
    }
}
