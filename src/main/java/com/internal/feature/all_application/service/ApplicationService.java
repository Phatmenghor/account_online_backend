package com.internal.feature.all_application.service;

import com.internal.feature.all_application.dto.filter.GetAllApplicationRequestDto;
import com.internal.feature.all_application.dto.request.ApplicationRequestDto;
import com.internal.feature.all_application.dto.resposne.AllApplicationResponseDto;
import com.internal.feature.all_application.dto.resposne.ApplicationResponseDto;
import com.internal.feature.all_application.dto.update.ApplicationUpdateDto;

import java.util.List;

public interface ApplicationService {
    
    ApplicationResponseDto createProject(ApplicationRequestDto requestDto);
    ApplicationResponseDto getProjectById(Long id);
    AllApplicationResponseDto getAllProject(GetAllApplicationRequestDto requestDto);
    List<ApplicationResponseDto> getAllListProject(GetAllApplicationRequestDto requestDto);
    ApplicationResponseDto updateProject(Long id, ApplicationUpdateDto updateDto);
    void deleteProject(Long id);
}
