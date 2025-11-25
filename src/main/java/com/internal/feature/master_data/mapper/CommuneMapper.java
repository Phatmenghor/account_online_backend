package com.internal.feature.master_data.mapper;

import com.internal.feature.master_data.dto.request.CommuneRequestDto;
import com.internal.feature.master_data.dto.response.CommuneResponseDto;
import com.internal.feature.master_data.dto.response.ClsCommuneDto;
import com.internal.feature.master_data.models.Commune;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {DistrictMapper.class})
public interface CommuneMapper {

    CommuneMapper INSTANCE = Mappers.getMapper(CommuneMapper.class);


    CommuneResponseDto toDto(Commune commune);

    List<CommuneResponseDto> toDtoList(List<Commune> communes);

    @Mapping(target = "district", ignore = true) // Handled in service
    Commune fromCreateDto(CommuneRequestDto request);

    @Mapping(target = "district", ignore = true) // Handled in service
    void updateFromDto(CommuneRequestDto request, @MappingTarget Commune commune);

    @Mapping(target = "districtCode", source = "district.districtCode")
    ClsCommuneDto toClsDto(Commune commune);
    List<ClsCommuneDto> toClsDtoList(List<Commune> communes);
}
