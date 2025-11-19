package com.internal.feature.all_application.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.all_application.dto.filter.GetAllApplicationRequestDto;
import com.internal.feature.all_application.dto.request.ApplicationRequestDto;
import com.internal.feature.all_application.dto.resposne.AllApplicationResponseDto;
import com.internal.feature.all_application.dto.resposne.ApplicationResponseDto;
import com.internal.feature.all_application.dto.update.ApplicationUpdateDto;
import com.internal.feature.all_application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {
    
    private final ApplicationService projectService;
    
    // Create TraineeReport
    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> createProject(@RequestBody ApplicationRequestDto requestDto) {
        log.info("Creating project: {}", requestDto.getProjectName());
        ApplicationResponseDto result = projectService.createProject(requestDto);
        
        ApiResponse<ApplicationResponseDto> response = new ApiResponse<>(
            "success", 
            "TraineeReport created successfully",
            result
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Get TraineeReport by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> getProjectById(@PathVariable Long id) {
        log.info("Getting project by ID: {}", id);
        ApplicationResponseDto result = projectService.getProjectById(id);
        
        ApiResponse<ApplicationResponseDto> response = new ApiResponse<>(
            "success", 
            "TraineeReport retrieved successfully",
            result
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Get All Projects with Pagination and Filters
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllApplicationResponseDto>> getAllProject(@RequestBody GetAllApplicationRequestDto requestDto) {
        log.info("Getting all projects with filters - page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());
        
        AllApplicationResponseDto result = projectService.getAllProject(requestDto);
        
        ApiResponse<AllApplicationResponseDto> response = new ApiResponse<>(
            "success", 
            "Projects retrieved successfully", 
            result
        );
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/all-list")
    public ResponseEntity<ApiResponse<List<ApplicationResponseDto>>> getAllListProject(@RequestBody GetAllApplicationRequestDto requestDto) {
        log.info("Getting all list projects with filters -- page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());

        List<ApplicationResponseDto> result = projectService.getAllListProject(requestDto);

        ApiResponse<List<ApplicationResponseDto>> response = new ApiResponse<>(
                "success",
                "Projects all retrieved successfully",
                result
        );

        return ResponseEntity.ok(response);
    }
    
    // Update TraineeReport
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> updateProject(
            @PathVariable Long id, 
            @RequestBody ApplicationUpdateDto updateDto) {
        log.info("Updating project ID: {}", id);
        ApplicationResponseDto result = projectService.updateProject(id, updateDto);
        
        ApiResponse<ApplicationResponseDto> response = new ApiResponse<>(
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