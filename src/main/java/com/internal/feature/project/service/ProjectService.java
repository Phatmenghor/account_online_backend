package com.internal.feature.project.service;

import com.internal.feature.project.dto.filter.GetAllProjectRequestDto;
import com.internal.feature.project.dto.request.ProjectRequestDto;
import com.internal.feature.project.dto.resposne.AllProjectResponseDto;
import com.internal.feature.project.dto.resposne.ProjectResponseDto;
import com.internal.feature.project.dto.update.ProjectUpdateDto;

public interface ProjectService {
    
    ProjectResponseDto createProject(ProjectRequestDto requestDto);
    ProjectResponseDto getProjectById(Long id);
    AllProjectResponseDto getAllProject(GetAllProjectRequestDto requestDto);
    ProjectResponseDto updateProject(Long id, ProjectUpdateDto updateDto);
    void deleteProject(Long id);
}
