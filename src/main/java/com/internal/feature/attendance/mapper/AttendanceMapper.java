package com.internal.feature.attendance.mapper;

import com.internal.feature.attendance.dto.request.AttendanceRequestDto;
import com.internal.feature.attendance.dto.request.AttendanceUpdateRequestDto;
import com.internal.feature.attendance.dto.resposne.AllAttendanceResponseDto;
import com.internal.feature.attendance.dto.resposne.AttendanceResponseDto;
import com.internal.feature.attendance.models.AttendanceEntity;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttendanceMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "userIdCard")
    @Mapping(source = "user.fullName", target = "userFullName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.position", target = "userPosition")
    @Mapping(source = "approvedBy.username", target = "approvedByIdCard")
    @Mapping(source = "approvedBy.fullName", target = "approvedByFullName")
    AttendanceResponseDto toDto(AttendanceEntity entity);

    List<AttendanceResponseDto> toDtoList(List<AttendanceEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "totalDays", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approvalNotes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AttendanceEntity toEntity(AttendanceRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalDays", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approvalNotes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(AttendanceUpdateRequestDto dto, @MappingTarget AttendanceEntity entity);

    @Named("mapToAllAttendanceResponseDto")
    default AllAttendanceResponseDto mapToAllAttendanceResponseDto(Page<AttendanceEntity> page) {
        List<AttendanceResponseDto> content = toDtoList(page.getContent());

        return AllAttendanceResponseDto.builder()
                .content(content)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
