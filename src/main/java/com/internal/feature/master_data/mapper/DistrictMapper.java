package com.internal.feature.master_data.mapper;

import com.internal.feature.master_data.dto.request.DistrictRequestDto;
import com.internal.feature.master_data.dto.response.DistrictResponseDto;
import com.internal.feature.master_data.dto.response.ClsDistrictDto;
import com.internal.feature.master_data.models.District;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {ProvinceMapper.class})
public interface DistrictMapper {

    DistrictMapper INSTANCE = Mappers.getMapper(DistrictMapper.class);
    DistrictResponseDto toDto(District district);

    List<DistrictResponseDto> toDtoList(List<District> districts);

    @Mapping(target = "province", ignore = true) // Handled in service
    District fromCreateDto(DistrictRequestDto request);

    @Mapping(target = "province", ignore = true) // Handled in service
    void updateFromDto(DistrictRequestDto request, @MappingTarget District district);

    @Mapping(target = "provinceCode", source = "province.provinceCode")
    ClsDistrictDto toClsDto(District district);
    List<ClsDistrictDto> toClsDtoList(List<District> districts);
}
