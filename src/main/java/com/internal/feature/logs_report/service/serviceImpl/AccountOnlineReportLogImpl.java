package com.internal.feature.logs_report.service.serviceImpl;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.logs_report.dto.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportLogResponse;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportProjection;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportResponse;
import com.internal.feature.logs_report.mapper.AccountOnlineReportMapper;
import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import com.internal.feature.logs_report.repository.AccountOnlineReportLogRepository;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountOnlineReportLogImpl implements AccountOnlineReportLogService {

    private final AccountOnlineReportLogRepository repository;
    private final AccountOnlineReportMapper accountOnlineReportMapper;

    @Override
    public void saveLogReport(String idNumber, OpenAccStatusEnum status, String remark) {
        repository.save(AccountOnlineReportLog.builder()
                .idNumber(idNumber)
                .status(status)
                .remark(remark)
                .build());
    }

    @Override
    public List<AccountOnlineReportResponse> getReportByDateRange(LocalDate fromDate, LocalDate toDate) {
        List<AccountOnlineReportProjection> projections = repository.getReportByDateRange(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay());
        return accountOnlineReportMapper.projectionsToResponses(projections);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAccountOpeningLog(String idNumber, OpenAccStatusEnum status, String stepInfo, Exception exception) {
        StringBuilder remark = new StringBuilder("Step: ").append(stepInfo);
        if (exception != null) {
            remark.append(" | Error: ").append(exception.getClass().getSimpleName())
                    .append(" | Message: ").append(exception.getMessage());
        }
        repository.save(AccountOnlineReportLog.builder()
                .idNumber(idNumber)
                .status(status)
                .remark(remark.toString())
                .build());
    }

    @Override
    public List<AccountOnlineReportLogResponse> getAllLogs(AccountOnlineReportLogDto request) {
        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().plusDays(1).atStartOfDay() : null;

        List<AccountOnlineReportLog> logs = repository.findByDateRangeAndStatuses(fromDateTime, toDateTime, request.getStatus());
        return accountOnlineReportMapper.toResponseList(logs);
    }

    @Override
    public PaginationResponse<AccountOnlineReportLogResponse> getLogsWithPagination(AccountOnlineReportLogDto request) {
        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().plusDays(1).atStartOfDay() : null;

        Page<AccountOnlineReportLog> page = repository.findByDateRangeAndStatusesPaged(
                fromDateTime, toDateTime, request.getStatus(),
                PageRequest.of(request.getPageNo() - 1, request.getPageSize()));

        return new PaginationResponse<>(
                accountOnlineReportMapper.toResponseList(page.getContent()),
                request.getPageNo(),
                request.getPageSize(),
                page.getTotalElements());
    }
}
