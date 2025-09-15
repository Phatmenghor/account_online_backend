package com.internal.feature.project.service.impl;

import com.internal.exceptions.error.ResourceNotFoundException;
import com.internal.feature.project.dto.filter.ProjectSearchRequestDto;
import com.internal.feature.project.dto.request.ProjectRequestDto;
import com.internal.feature.project.dto.resposne.ProjectResponseDto;
import com.internal.feature.project.dto.update.ProjectUpdateDto;
import com.internal.feature.project.mapper.ProjectMapper;
import com.internal.feature.project.models.Project;
import com.internal.feature.project.repository.ProjectRepository;
import com.internal.feature.project.service.ProjectService;
import com.internal.feature.project.specification.ProjectSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectRepository repository;
    private final ProjectMapper mapper;
    
    @Override
    public ProjectResponseDto createProject(ProjectRequestDto requestDto) {
        log.info("Creating new project: {}", requestDto.getProjectName());
        
        Project entity = mapper.toEntity(requestDto);
        Project savedEntity = repository.save(entity);
        
        return mapper.toResponseDto(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id) {
        log.info("Fetching project by ID: {}", id);
        
        Project entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
        
        return mapper.toResponseDto(entity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> searchProjects(ProjectSearchRequestDto requestDto) {
        log.info("Searching projects with filters and pagination");
        
        // Convert page number from 1-based to 0-based
        int pageNo = Math.max(requestDto.getPageNo() - 1, 0);
        int pageSize = Math.max(requestDto.getPageSize(), 1);
        
        // Create sort with multiple fields
        Sort sort = createSort(requestDto.getSortBy(), requestDto.getSortDir());
        
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        
        // Create specification for filtering
        Specification<Project> specification = ProjectSpecification.createSpecification(requestDto.getSearch());
        
        Page<Project> entities = repository.findAll(specification, pageable);
        
        return entities.map(mapper::toResponseDto);
    }
    
    private Sort createSort(String sortBy, String sortDir) {
        Sort.Direction direction = Sort.Direction.DESC;
        if ("ASC".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.ASC;
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        
        // Primary sort field
        if (sortBy != null && !sortBy.isEmpty()) {
            orders.add(new Sort.Order(direction, sortBy));
        } else {
            orders.add(new Sort.Order(Sort.Direction.DESC, "createdAt"));
        }
        
        // Secondary sort fields for consistent ordering
        if (!"createdAt".equals(sortBy)) {
            orders.add(new Sort.Order(Sort.Direction.DESC, "createdAt"));
        }
        if (!"id".equals(sortBy)) {
            orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        }
        
        return Sort.by(orders);
    }
    
    @Override
    public ProjectResponseDto updateProject(Long id, ProjectUpdateDto updateDto) {
        log.info("Updating project with ID: {}", id);
        
        Project existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
        
        mapper.updateEntityFromDto(updateDto, existingEntity);
        Project updatedEntity = repository.save(existingEntity);
        
        return mapper.toResponseDto(updatedEntity);
    }
    
    @Override
    public void deleteProject(Long id) {
        log.info("Deleting project with ID: {}", id);
        
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with ID: " + id);
        }
        
        repository.deleteById(id);
    }
}