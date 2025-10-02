package com.internal.feature.all_application.mapper;

import com.internal.feature.all_application.dto.request.ApplicationRequestDto;
import com.internal.feature.all_application.dto.resposne.AllApplicationResponseDto;
import com.internal.feature.all_application.dto.resposne.ApplicationResponseDto;
import com.internal.feature.all_application.dto.update.ApplicationUpdateDto;
import com.internal.feature.all_application.models.Application;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    ApplicationResponseDto toResponseDto(Application entity);
    
    Application toEntity(ApplicationRequestDto requestDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ApplicationUpdateDto updateDto, @MappingTarget Application entity);
    
    @Named("mapToListDto")
    default AllApplicationResponseDto mapToListDto(List<ApplicationResponseDto> content, Page<Application> projectPage) {
        AllApplicationResponseDto projectResponse = new AllApplicationResponseDto();
        projectResponse.setContent(content);
        projectResponse.setPageNo(projectPage.getNumber() + 1); // Convert back to 1-based
        projectResponse.setPageSize(projectPage.getSize());
        projectResponse.setTotalElements(projectPage.getTotalElements());
        projectResponse.setTotalPages(projectPage.getTotalPages());
        projectResponse.setLast(projectPage.isLast());
        return projectResponse;
    }
}