package com.internal.feature.project.service.impl;

import com.internal.exceptions.error.ResourceNotFoundException;
import com.internal.feature.project.dto.filter.GetAllProjectRequestDto;
import com.internal.feature.project.dto.request.ProjectRequestDto;
import com.internal.feature.project.dto.resposne.AllProjectResponseDto;
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

import java.util.List;
import java.util.stream.Collectors;

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
    
//    @Override
//    @Transactional(readOnly = true)
//    public AllProjectResponseDto getAllProject(GetAllProjectRequestDto requestDto) {
//        log.debug("Getting projects with pageNo={}, pageSize={}, search={}",
//                requestDto.getPageNo(), requestDto.getPageSize(), requestDto.getSearch());
//
//        GetAllProjectRequestDto projectRequestDto = new GetAllProjectRequestDto(
//                requestDto.getSearch(),
//                Math.max(requestDto.getPageNo() - 1, 0),
//                Math.max(requestDto.getPageSize(), 1)
//        );
//
//        Pageable pageable = PageRequest.of(
//                projectRequestDto.getPageNo(),
//                projectRequestDto.getPageSize(),
//                Sort.by(Sort.Direction.DESC, "createdAt")
//        );
//
//        // Create specification for filtering
//        Specification<Project> specification = ProjectSpecification.createSpecification(projectRequestDto.getSearch());
//
//        Page<Project> projectPage = repository.findAll(specification, pageable);
//
//        List<ProjectResponseDto> content = projectPage.getContent()
//                .stream()
//                .map(mapper::toResponseDto)
//                .collect(Collectors.toList());
//
//        return mapper.mapToListDto(content, projectPage);
//    }

    @Override
    @Transactional(readOnly = true)
    public AllProjectResponseDto getAllProject(GetAllProjectRequestDto requestDto) {
        log.debug("Getting projects with pageNo={}, pageSize={}, search={}, projectStatus={}",
                requestDto.getPageNo(), requestDto.getPageSize(), requestDto.getSearch(), requestDto.getProjectStatus());

        GetAllProjectRequestDto projectRequestDto = new GetAllProjectRequestDto(
                requestDto.getSearch(),
                Math.max(requestDto.getPageNo() - 1, 0),
                Math.max(requestDto.getPageSize(), 1),
                requestDto.getProjectStatus()  // Include project status
        );

        Pageable pageable = PageRequest.of(
                projectRequestDto.getPageNo(),
                projectRequestDto.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Create specification for filtering including project status
        Specification<Project> specification = ProjectSpecification.createSpecification(
                projectRequestDto.getSearch(),
                projectRequestDto.getProjectStatus()
        );

        Page<Project> projectPage = repository.findAll(specification, pageable);

        List<ProjectResponseDto> content = projectPage.getContent()
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());

        return mapper.mapToListDto(content, projectPage);
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
