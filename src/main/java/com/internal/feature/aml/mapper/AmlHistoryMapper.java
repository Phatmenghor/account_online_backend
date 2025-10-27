package com.internal.feature.aml.mapper;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.AmlHistoryRequestDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AmlHistoryDto;
import com.internal.feature.aml.model.AmlHistory;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.auth.dto.response.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AmlHistoryMapper {

    default AmlHistory fromCreateDto(AmlHistoryRequestDto request, UserMapper userMapper) {
        if (request == null) return null;

        AmlHistory history = new AmlHistory();
        history.setOriginalRequest(request.getOriginalRequest());
        history.setOriginalResponse(request.getOriginalResponse());
        history.setOldStatus(request.getOldStatus());
        history.setNewStatus(request.getNewStatus());

        if (request.getChangedBy() != null) {
            history.setChangedBy(userMapper.mapToEntity(request.getChangedBy()));
        }

        return history;
    }

    /**
     * Create history entry from AmlStatus change
     */
    default AmlHistory createHistoryFromStatusChange(
            String originalRequest,
            String originalResponse,
            AmlStatusEnum oldStatus,
            AmlStatusEnum newStatus,
            UserEntity changedBy
    ) {
        AmlHistory history = new AmlHistory();
        history.setOriginalRequest(originalRequest);
        history.setOriginalResponse(originalResponse);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        return history;
    }

    /**
     * Create history entry from AmlStatus entity
     */
    default AmlHistory createHistoryFromStatus(
            AmlStatus status,
            AmlStatusEnum oldStatus,
            UserEntity changedBy
    ) {
        return createHistoryFromStatusChange(
                status.getOriginalRequest(),
                status.getOriginalResponse(),
                oldStatus,
                status.getStatus(),
                changedBy
        );
    }

    default AmlHistoryDto toDto(AmlHistory history, UserMapper userMapper) {
        if (history == null) return null;

        UserResponseDto userDto = null;
        UserEntity changedBy = history.getChangedBy();
        if (changedBy != null) {
            userDto = userMapper.mapToDto(changedBy);
        }

        return AmlHistoryDto.builder()
                .id(history.getId())
                .originalRequest(history.getOriginalRequest())
                .originalResponse(history.getOriginalResponse())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedBy(userDto)
                .build();
    }

    @Named("mapToListDto")
    default AllAmlHistoryResponseDto mapToListDto(List<AmlHistoryDto> content, Page<AmlHistory> histories) {
        AllAmlHistoryResponseDto response = new AllAmlHistoryResponseDto();
        response.setContent(content);
        response.setPageNo(histories.getNumber() + 1);
        response.setPageSize(histories.getSize());
        response.setTotalElements(histories.getTotalElements());
        response.setTotalPages(histories.getTotalPages());
        response.setLast(histories.isLast());
        return response;
    }
}