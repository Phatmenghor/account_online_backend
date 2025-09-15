package com.internal.feature.project.controller;

import com.internal.feature.project.dto.filter.ProjectSearchRequestDto;
import com.internal.feature.project.dto.request.ProjectRequestDto;
import com.internal.feature.project.dto.resposne.ProjectResponseDto;
import com.internal.feature.project.dto.update.ProjectUpdateDto;
import com.internal.feature.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {
    
    private final ProjectService projectService;
    
    // Create Project
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody ProjectRequestDto requestDto) {
        log.info("Creating project: {}", requestDto.getProjectName());
        ProjectResponseDto result = projectService.createProject(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    // Get Project by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id) {
        log.info("Getting project by ID: {}", id);
        ProjectResponseDto result = projectService.getProjectById(id);
        return ResponseEntity.ok(result);
    }
    
    // Search Projects with Pagination and Filters using Specification
    @PostMapping("/get-all")
    public ResponseEntity<Page<ProjectResponseDto>> searchProjects(@RequestBody ProjectSearchRequestDto requestDto) {
        log.info("Searching projects with filters - page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());
        
        Page<ProjectResponseDto> result = projectService.searchProjects(requestDto);
        return ResponseEntity.ok(result);
    }
    
    // Update Project
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long id, 
            @RequestBody ProjectUpdateDto updateDto) {
        log.info("Updating project ID: {}", id);
        ProjectResponseDto result = projectService.updateProject(id, updateDto);
        return ResponseEntity.ok(result);
    }
    
    // Delete Project
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("Deleting project ID: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}