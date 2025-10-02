package com.internal.feature.all_application.service.impl;

import com.internal.exceptions.error.ResourceNotFoundException;
import com.internal.feature.all_application.dto.filter.GetAllApplicationRequestDto;
import com.internal.feature.all_application.dto.request.ApplicationRequestDto;
import com.internal.feature.all_application.dto.resposne.AllApplicationResponseDto;
import com.internal.feature.all_application.dto.resposne.ApplicationResponseDto;
import com.internal.feature.all_application.dto.update.ApplicationUpdateDto;
import com.internal.feature.all_application.mapper.ApplicationMapper;
import com.internal.feature.all_application.models.Application;
import com.internal.feature.all_application.repository.ApplicationRepository;
import com.internal.feature.all_application.service.ApplicationService;
import com.internal.feature.all_application.specification.ApplicationSpecification;
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
public class ApplicationServiceImpl implements ApplicationService {
    
    private final ApplicationRepository repository;
    private final ApplicationMapper mapper;
    
    @Override
    public ApplicationResponseDto createProject(ApplicationRequestDto requestDto) {
        log.info("Creating new project: {}", requestDto.getProjectName());
        
        Application entity = mapper.toEntity(requestDto);
        Application savedEntity = repository.save(entity);
        
        return mapper.toResponseDto(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDto getProjectById(Long id) {
        log.info("Fetching project by ID: {}", id);
        
        Application entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));
        
        return mapper.toResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AllApplicationResponseDto getAllProject(GetAllApplicationRequestDto requestDto) {
        log.debug("Getting projects with pageNo={}, pageSize={}, search={}, projectStatus={}",
                requestDto.getPageNo(), requestDto.getPageSize(), requestDto.getSearch(), requestDto.getApplicationStatus());

        GetAllApplicationRequestDto projectRequestDto = new GetAllApplicationRequestDto(
                requestDto.getSearch(),
                Math.max(requestDto.getPageNo() - 1, 0),
                Math.max(requestDto.getPageSize(), 1),
                requestDto.getApplicationStatus()
        );

        Pageable pageable = PageRequest.of(
                projectRequestDto.getPageNo(),
                projectRequestDto.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Create specification for filtering including project status
        Specification<Application> specification = ApplicationSpecification.createSpecification(
                projectRequestDto.getSearch(),
                projectRequestDto.getApplicationStatus()
        );

        Page<Application> projectPage = repository.findAll(specification, pageable);

        List<ApplicationResponseDto> content = projectPage.getContent()
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());

        return mapper.mapToListDto(content, projectPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getAllListProject(GetAllApplicationRequestDto requestDto) {
        log.debug("Getting projects with pageNo={}, pageSize={}, search={}, projectStatus={}",
                requestDto.getPageNo(), requestDto.getPageSize(), requestDto.getSearch(), requestDto.getApplicationStatus());


        Specification<Application> specification = ApplicationSpecification.createSpecification(
                requestDto.getSearch(),
                requestDto.getApplicationStatus()
        );

        List<Application> projects = repository.findAll(
                specification,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return projects.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public ApplicationResponseDto updateProject(Long id, ApplicationUpdateDto updateDto) {
        log.info("Updating project with ID: {}", id);
        
        Application existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));
        
        mapper.updateEntityFromDto(updateDto, existingEntity);
        Application updatedEntity = repository.save(existingEntity);
        
        return mapper.toResponseDto(updatedEntity);
    }
    
    @Override
    public void deleteProject(Long id) {
        log.info("Deleting project with ID: {}", id);
        
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with ID: " + id);
        }
        
        repository.deleteById(id);
    }
}
