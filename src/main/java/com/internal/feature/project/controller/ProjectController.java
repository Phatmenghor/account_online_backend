package com.internal.feature.project.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.project.dto.filter.GetAllProjectRequestDto;
import com.internal.feature.project.dto.request.ProjectRequestDto;
import com.internal.feature.project.dto.resposne.AllProjectResponseDto;
import com.internal.feature.project.dto.resposne.ProjectResponseDto;
import com.internal.feature.project.dto.update.ProjectUpdateDto;
import com.internal.feature.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {
    
    private final ProjectService projectService;
    
    // Create TraineeReport
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponseDto>> createProject(@RequestBody ProjectRequestDto requestDto) {
        log.info("Creating project: {}", requestDto.getProjectName());
        ProjectResponseDto result = projectService.createProject(requestDto);
        
        ApiResponse<ProjectResponseDto> response = new ApiResponse<>(
            "success", 
            "TraineeReport created successfully",
            result
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Get TraineeReport by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProjectById(@PathVariable Long id) {
        log.info("Getting project by ID: {}", id);
        ProjectResponseDto result = projectService.getProjectById(id);
        
        ApiResponse<ProjectResponseDto> response = new ApiResponse<>(
            "success", 
            "TraineeReport retrieved successfully",
            result
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Get All Projects with Pagination and Filters
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllProjectResponseDto>> getAllProject(@RequestBody GetAllProjectRequestDto requestDto) {
        log.info("Getting all projects with filters - page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());
        
        AllProjectResponseDto result = projectService.getAllProject(requestDto);
        
        ApiResponse<AllProjectResponseDto> response = new ApiResponse<>(
            "success", 
            "Projects retrieved successfully", 
            result
        );
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/all-list")
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getAllListProject(@RequestBody GetAllProjectRequestDto requestDto) {
        log.info("Getting all list projects with filters - page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());

        List<ProjectResponseDto> result = projectService.getAllListProject(requestDto);

        ApiResponse<List<ProjectResponseDto>> response = new ApiResponse<>(
                "success",
                "Projects all retrieved successfully",
                result
        );

        return ResponseEntity.ok(response);
    }
    
    // Update TraineeReport
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProject(
            @PathVariable Long id, 
            @RequestBody ProjectUpdateDto updateDto) {
        log.info("Updating project ID: {}", id);
        ProjectResponseDto result = projectService.updateProject(id, updateDto);
        
        ApiResponse<ProjectResponseDto> response = new ApiResponse<>(
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