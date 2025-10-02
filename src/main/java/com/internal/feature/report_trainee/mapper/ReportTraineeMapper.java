package com.internal.feature.report_trainee.mapper;

import com.internal.feature.report_trainee.dto.request.ReportTraineeRequestDto;
import com.internal.feature.report_trainee.dto.resposne.AllReportTraineeResponseDto;
import com.internal.feature.report_trainee.dto.resposne.ReportTraineeResponseDto;
import com.internal.feature.report_trainee.dto.update.ReportTraineeUpdateDto;
import com.internal.feature.report_trainee.models.TraineeReport;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportTraineeMapper {

    ReportTraineeResponseDto toResponseDto(TraineeReport entity);
    
    TraineeReport toEntity(ReportTraineeRequestDto requestDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ReportTraineeUpdateDto updateDto, @MappingTarget TraineeReport entity);
    
    @Named("mapToListDto")
    default AllReportTraineeResponseDto mapToListDto(List<ReportTraineeResponseDto> content, Page<TraineeReport> projectPage) {
        AllReportTraineeResponseDto projectResponse = new AllReportTraineeResponseDto();
        projectResponse.setContent(content);
        projectResponse.setPageNo(projectPage.getNumber() + 1); // Convert back to 1-based
        projectResponse.setPageSize(projectPage.getSize());
        projectResponse.setTotalElements(projectPage.getTotalElements());
        projectResponse.setTotalPages(projectPage.getTotalPages());
        projectResponse.setLast(projectPage.isLast());
        return projectResponse;
    }
}