package com.internal.feature.logs_report.mapper;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportLogResponse;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportProjection;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportResponse;
import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AccountOnlineReportMapper {

    // Map single entity to response
    AccountOnlineReportLogResponse toResponse(AccountOnlineReportLog entity);

    // Map list of entities to list of responses
    List<AccountOnlineReportLogResponse> toResponseList(List<AccountOnlineReportLog> entities);

    default List<AccountOnlineReportResponse> projectionsToResponses(List<AccountOnlineReportProjection> projections) {
        // Group by date
        Map<LocalDate, Map<OpenAccStatusEnum, Long>> groupedData = new HashMap<>();
        
        for (AccountOnlineReportProjection projection : projections) {
            LocalDate date = projection.getDate();
            OpenAccStatusEnum status = projection.getStatus();
            Long count = projection.getCount();

            groupedData
                .computeIfAbsent(date, k -> new HashMap<>())
                .put(status, count);
        }

        // Convert to response DTOs
        return groupedData.entrySet().stream()
            .map(entry -> AccountOnlineReportResponse.builder()
                .date(entry.getKey())
                .successCount(entry.getValue().getOrDefault(OpenAccStatusEnum.SUCCESS, 0L))
                .failureCount(entry.getValue().getOrDefault(OpenAccStatusEnum.FAILURE, 0L))
                .amlCount(entry.getValue().getOrDefault(OpenAccStatusEnum.AML, 0L))
                .build())
            .sorted(Comparator.comparing(AccountOnlineReportResponse::getDate))
            .collect(Collectors.toList());
    }
}
