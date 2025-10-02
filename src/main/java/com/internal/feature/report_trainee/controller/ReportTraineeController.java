package com.internal.feature.report_trainee.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.report_trainee.dto.filter.GetAllReportTraineeRequestDto;
import com.internal.feature.report_trainee.dto.request.ReportTraineeRequestDto;
import com.internal.feature.report_trainee.dto.resposne.AllReportTraineeResponseDto;
import com.internal.feature.report_trainee.dto.resposne.ReportTraineeResponseDto;
import com.internal.feature.report_trainee.dto.update.ReportTraineeUpdateDto;
import com.internal.feature.report_trainee.service.ReportTraineeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-trainee")
@RequiredArgsConstructor
@Slf4j
public class ReportTraineeController {
    
    private final ReportTraineeService projectService;
    
    // Create TraineeReport
    @PostMapping
    public ResponseEntity<ApiResponse<ReportTraineeResponseDto>> createProject(@RequestBody ReportTraineeRequestDto requestDto) {
        log.info("Creating project");
        ReportTraineeResponseDto result = projectService.createProject(requestDto);
        
        ApiResponse<ReportTraineeResponseDto> response = new ApiResponse<>(
            "success", 
            "TraineeReport created successfully",
            result
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Get TraineeReport by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportTraineeResponseDto>> getProjectById(@PathVariable Long id) {
        log.info("Getting project by ID: {}", id);
        ReportTraineeResponseDto result = projectService.getProjectById(id);
        
        ApiResponse<ReportTraineeResponseDto> response = new ApiResponse<>(
            "success", 
            "TraineeReport retrieved successfully",
            result
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Get All Projects with Pagination and Filters
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllReportTraineeResponseDto>> getAllProject(@RequestBody GetAllReportTraineeRequestDto requestDto) {
        log.info("Getting all projects with filters - page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());
        
        AllReportTraineeResponseDto result = projectService.getAllProject(requestDto);
        
        ApiResponse<AllReportTraineeResponseDto> response = new ApiResponse<>(
            "success", 
            "Projects retrieved successfully", 
            result
        );
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/all-list")
    public ResponseEntity<ApiResponse<List<ReportTraineeResponseDto>>> getAllListProject(@RequestBody GetAllReportTraineeRequestDto requestDto) {
        log.info("Getting all list projects with filters - page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());

        List<ReportTraineeResponseDto> result = projectService.getAllListProject(requestDto);

        ApiResponse<List<ReportTraineeResponseDto>> response = new ApiResponse<>(
                "success",
                "Projects all retrieved successfully",
                result
        );

        return ResponseEntity.ok(response);
    }
    
    // Update TraineeReport
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportTraineeResponseDto>> updateProject(
            @PathVariable Long id, 
            @RequestBody ReportTraineeUpdateDto updateDto) {
        log.info("Updating project ID: {}", id);
        ReportTraineeResponseDto result = projectService.updateProject(id, updateDto);
        
        ApiResponse<ReportTraineeResponseDto> response = new ApiResponse<>(
            "success", 
            "TraineeReport updated successfully",
            result
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Delete TraineeReport
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id) {
        log.info("Deleting project ID: {}", id);
        projectService.deleteProject(id);
        
        ApiResponse<String> response = new ApiResponse<>(
            "success", 
            "TraineeReport deleted successfully",
            "TraineeReport with ID " + id + " has been deleted"
        );
        
        return ResponseEntity.ok(response);
    }
}