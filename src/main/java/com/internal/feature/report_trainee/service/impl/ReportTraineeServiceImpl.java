package com.internal.feature.report_trainee.service.impl;

import com.internal.exceptions.error.ResourceNotFoundException;
import com.internal.feature.report_trainee.dto.filter.GetAllReportTraineeRequestDto;
import com.internal.feature.report_trainee.dto.request.ReportTraineeRequestDto;
import com.internal.feature.report_trainee.dto.resposne.AllReportTraineeResponseDto;
import com.internal.feature.report_trainee.dto.resposne.ReportTraineeResponseDto;
import com.internal.feature.report_trainee.dto.update.ReportTraineeUpdateDto;
import com.internal.feature.report_trainee.mapper.ReportTraineeMapper;
import com.internal.feature.report_trainee.models.TraineeReport;
import com.internal.feature.report_trainee.repository.ReportTraineeRepository;
import com.internal.feature.report_trainee.service.ReportTraineeService;
import com.internal.feature.report_trainee.specification.ApplicationSpecification;
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
public class ReportTraineeServiceImpl implements ReportTraineeService {
    
    private final ReportTraineeRepository repository;
    private final ReportTraineeMapper mapper;
    
    @Override
    public ReportTraineeResponseDto createProject(ReportTraineeRequestDto requestDto) {
        log.info("Creating new project:");
        
        TraineeReport entity = mapper.toEntity(requestDto);
        TraineeReport savedEntity = repository.save(entity);
        
        return mapper.toResponseDto(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReportTraineeResponseDto getProjectById(Long id) {
        log.info("Fetching project by ID: {}", id);
        
        TraineeReport entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TraineeReport not found with ID: " + id));
        
        return mapper.toResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AllReportTraineeResponseDto getAllProject(GetAllReportTraineeRequestDto requestDto) {
        log.debug("Getting all projects with pageNo={}, pageSize={}, search={}",
                requestDto.getPageNo(), requestDto.getPageSize(), requestDto.getSearch());

        GetAllReportTraineeRequestDto projectRequestDto = new GetAllReportTraineeRequestDto(
                requestDto.getSearch(),
                Math.max(requestDto.getPageNo() - 1, 0),
                Math.max(requestDto.getPageSize(), 1)
        );

        Pageable pageable = PageRequest.of(
                projectRequestDto.getPageNo(),
                projectRequestDto.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Create specification for filtering including project status
        Specification<TraineeReport> specification = ApplicationSpecification.createSpecification(
                projectRequestDto.getSearch()
        );

        Page<TraineeReport> projectPage = repository.findAll(specification, pageable);

        List<ReportTraineeResponseDto> content = projectPage.getContent()
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());

        return mapper.mapToListDto(content, projectPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportTraineeResponseDto> getAllListProject(GetAllReportTraineeRequestDto requestDto) {
        log.debug("Getting projects with pageNo={}, pageSize={}, search={}",
                requestDto.getPageNo(), requestDto.getPageSize(), requestDto.getSearch());


        Specification<TraineeReport> specification = ApplicationSpecification.createSpecification(
                requestDto.getSearch()
        );

        List<TraineeReport> projects = repository.findAll(
                specification,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return projects.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public ReportTraineeResponseDto updateProject(Long id, ReportTraineeUpdateDto updateDto) {
        log.info("Updating project with ID: {}", id);
        
        TraineeReport existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TraineeReport not found with ID: " + id));
        
        mapper.updateEntityFromDto(updateDto, existingEntity);
        TraineeReport updatedEntity = repository.save(existingEntity);
        
        return mapper.toResponseDto(updatedEntity);
    }
    
    @Override
    public void deleteProject(Long id) {
        log.info("Deleting project with ID: {}", id);
        
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TraineeReport not found with ID: " + id);
        }
        
        repository.deleteById(id);
    }
}
