package com.internal.feature.master_data.mapper;

import com.internal.feature.master_data.dto.request.VillageRequestDto;
import com.internal.feature.master_data.dto.response.VillageResponseDto;
import com.internal.feature.master_data.dto.response.ClsVillageDto;
import com.internal.feature.master_data.models.Village;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {CommuneMapper.class})
public interface VillageMapper {

    VillageMapper INSTANCE = Mappers.getMapper(VillageMapper.class);

    VillageResponseDto toDto(Village village);

    List<VillageResponseDto> toDtoList(List<Village> villages);

    @Mapping(target = "commune", ignore = true) // Handled in service
    Village fromCreateDto(VillageRequestDto request);

    @Mapping(target = "commune", ignore = true) // Handled in service
    void updateFromDto(VillageRequestDto request, @MappingTarget Village village);

    @Mapping(target = "communeCode", source = "commune.communeCode")
    ClsVillageDto toClsDto(Village village);
    List<ClsVillageDto> toClsDtoList(List<Village> villages);
}
