package com.internal.feature.report_trainee.service;

import com.internal.feature.report_trainee.dto.filter.GetAllReportTraineeRequestDto;
import com.internal.feature.report_trainee.dto.request.ReportTraineeRequestDto;
import com.internal.feature.report_trainee.dto.resposne.AllReportTraineeResponseDto;
import com.internal.feature.report_trainee.dto.resposne.ReportTraineeResponseDto;
import com.internal.feature.report_trainee.dto.update.ReportTraineeUpdateDto;

import java.util.List;

public interface ReportTraineeService {
    
    ReportTraineeResponseDto createProject(ReportTraineeRequestDto requestDto);
    ReportTraineeResponseDto getProjectById(Long id);
    AllReportTraineeResponseDto getAllProject(GetAllReportTraineeRequestDto requestDto);
    List<ReportTraineeResponseDto> getAllListProject(GetAllReportTraineeRequestDto requestDto);
    ReportTraineeResponseDto updateProject(Long id, ReportTraineeUpdateDto updateDto);
    void deleteProject(Long id);
}
