package com.internal.feature.master_data.mapper;

import com.internal.feature.master_data.dto.request.ProvinceRequestDto;
import com.internal.feature.master_data.dto.response.ProvinceResponseDto;
import com.internal.feature.master_data.dto.response.ClsProvinceDto;
import com.internal.feature.master_data.models.Province;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProvinceMapper {

    ProvinceMapper INSTANCE = Mappers.getMapper(ProvinceMapper.class);

    ProvinceResponseDto toDto(Province province);

    List<ProvinceResponseDto> toDtoList(List<Province> provinces);

    Province fromCreateDto(ProvinceRequestDto request);

    void updateFromDto(ProvinceRequestDto request, @MappingTarget Province province);

    ClsProvinceDto toClsDto(Province province);
    List<ClsProvinceDto> toClsDtoList(List<Province> provinces);
}
