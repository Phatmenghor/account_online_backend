package com.internal.feature.master_data.mapper;

import com.internal.feature.master_data.dto.request.BranchRequestDto;
import com.internal.feature.master_data.dto.response.BranchResponseDto;
import com.internal.feature.master_data.models.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BranchMapper {

    BranchMapper INSTANCE = Mappers.getMapper(BranchMapper.class);

    BranchResponseDto toDto(Branch branch);

    List<BranchResponseDto> toDtoList(List<Branch> branches);

    Branch fromCreateDto(BranchRequestDto request);

    void updateFromDto(BranchRequestDto request, @MappingTarget Branch branch);

    @org.mapstruct.Mapping(target = "branchID", source = "branchCode")
    @org.mapstruct.Mapping(target = "branchkh", source = "branchKh")
    com.internal.feature.master_data.dto.response.ClsBranchDto toClsDto(Branch branch);
    
    List<com.internal.feature.master_data.dto.response.ClsBranchDto> toClsDtoList(List<Branch> branches);
}
