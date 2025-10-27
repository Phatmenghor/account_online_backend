package com.internal.feature.aml.mapper;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.UpdateAmlRequestDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AmlStatusMapper {

    default AmlStatus fromCreateDto(CreateAmlRequestDto request, UserMapper userMapper) {
        if (request == null) return null;

        AmlStatus status = new AmlStatus();
        status.setOriginalRequest(request.getOriginalRequest());
        status.setOriginalResponse(request.getOriginalResponse());
        status.setStatus(request.getStatus() != null ? request.getStatus() : AmlStatusEnum.PENDING);

        if (request.getApprovedBy() != null) {
            status.setApprovedBy(userMapper.mapToEntity(request.getApprovedBy()));
        }
        if (request.getRejectedBy() != null) {
            status.setRejectedBy(userMapper.mapToEntity(request.getRejectedBy()));
        }

        return status;
    }

    default void updateFromDto(UpdateAmlRequestDto request, @MappingTarget AmlStatus status, UserMapper userMapper) {
        if (request == null || status == null) return;

        if (request.getStatus() != null) status.setStatus(request.getStatus());

        if (request.getApprovedBy() != null) {
            status.setApprovedBy(userMapper.mapToEntity(request.getApprovedBy()));
        }
        if (request.getRejectedBy() != null) {
            status.setRejectedBy(userMapper.mapToEntity(request.getRejectedBy()));
        }
    }

    default AmlStatusDto toStatusDto(AmlStatus status, UserMapper userMapper) {
        if (status == null) return null;

        AmlStatusDto dto = new AmlStatusDto();
        dto.setId(status.getId());
        dto.setOriginalRequest(status.getOriginalRequest());
        dto.setOriginalResponse(status.getOriginalResponse());
        dto.setStatus(status.getStatus());

        // Map approvedBy and rejectedBy using UserMapper
        dto.setApprovedBy(status.getApprovedBy() != null ? userMapper.mapToDto(status.getApprovedBy()) : null);
        dto.setRejectedBy(status.getRejectedBy() != null ? userMapper.mapToDto(status.getRejectedBy()) : null);

        return dto;
    }

    @Named("mapToListDto")
    default AllAmlResponseDto mapToListDto(List<AmlStatusDto> content, Page<AmlStatus> statuses) {
        AllAmlResponseDto amlDtoList = new AllAmlResponseDto();
        amlDtoList.setContent(content);
        amlDtoList.setPageNo(statuses.getNumber() + 1);
        amlDtoList.setPageSize(statuses.getSize());
        amlDtoList.setTotalElements(statuses.getTotalElements());
        amlDtoList.setTotalPages(statuses.getTotalPages());
        amlDtoList.setLast(statuses.isLast());
        return amlDtoList;
    }
}
