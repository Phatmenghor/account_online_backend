package com.internal.feature.project.mapper;

import com.internal.feature.project.dto.request.ProjectRequestDto;
import com.internal.feature.project.dto.resposne.ProjectResponseDto;
import com.internal.feature.project.dto.update.ProjectUpdateDto;
import com.internal.feature.project.models.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    
    ProjectResponseDto toResponseDto(Project entity);
    Project toEntity(ProjectRequestDto requestDto);
    void updateEntityFromDto(ProjectUpdateDto updateDto, @MappingTarget Project entity);
}