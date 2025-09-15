package com.internal.feature.project.mapper;

import com.internal.feature.project.dto.request.ProjectRequestDto;
import com.internal.feature.project.dto.resposne.AllProjectResponseDto;
import com.internal.feature.project.dto.resposne.ProjectResponseDto;
import com.internal.feature.project.dto.update.ProjectUpdateDto;
import com.internal.feature.project.models.Project;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "projectName", target = "projectName")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "hostServer", target = "hostServer")
    @Mapping(source = "hostPort", target = "hostPort")
    @Mapping(source = "dbName", target = "dbName")
    @Mapping(source = "dbType", target = "dbType")
    @Mapping(source = "dbServer", target = "dbServer")
    @Mapping(source = "remark", target = "remark")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    ProjectResponseDto toResponseDto(Project entity);
    
    Project toEntity(ProjectRequestDto requestDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProjectUpdateDto updateDto, @MappingTarget Project entity);
    
    @Named("mapToListDto")
    default AllProjectResponseDto mapToListDto(List<ProjectResponseDto> content, Page<Project> projectPage) {
        AllProjectResponseDto projectResponse = new AllProjectResponseDto();
        projectResponse.setContent(content);
        projectResponse.setPageNo(projectPage.getNumber() + 1); // Convert back to 1-based
        projectResponse.setPageSize(projectPage.getSize());
        projectResponse.setTotalElements(projectPage.getTotalElements());
        projectResponse.setTotalPages(projectPage.getTotalPages());
        projectResponse.setLast(projectPage.isLast());
        return projectResponse;
    }
}