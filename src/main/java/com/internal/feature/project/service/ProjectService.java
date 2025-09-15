package com.internal.feature.project.service;

import com.internal.feature.project.dto.filter.ProjectSearchRequestDto;
import com.internal.feature.project.dto.request.ProjectRequestDto;
import com.internal.feature.project.dto.resposne.ProjectResponseDto;
import com.internal.feature.project.dto.update.ProjectUpdateDto;
import org.springframework.data.domain.Page;

public interface ProjectService {
    
    ProjectResponseDto createProject(ProjectRequestDto requestDto);
    ProjectResponseDto getProjectById(Long id);
    Page<ProjectResponseDto> searchProjects(ProjectSearchRequestDto requestDto);
    ProjectResponseDto updateProject(Long id, ProjectUpdateDto updateDto);
    void deleteProject(Long id);
}
