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
import com.internal.feature.telegram_alerts.service.AlertsOpenAccOnlineService;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountOnlineReportLogImpl implements AccountOnlineReportLogService {

    private final AccountOnlineReportLogRepository repository;
    private final AccountOnlineReportMapper accountOnlineReportMapper;
    private final AlertsOpenAccOnlineService alertsOpenAccOnlineService;

    @Override
    public void saveLogReport(String idNumber, OpenAccStatusEnum status, String remark) {
        log.info("Saving account online report log - ID number: {}, Status: {}", idNumber, status);

        AccountOnlineReportLog logEntry = AccountOnlineReportLog.builder()
                .idNumber(idNumber)
                .status(status)
                .remark(remark)
                .build();

        repository.save(logEntry);
    }

    @Override
    public List<AccountOnlineReportResponse> getReportByDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.plusDays(1).atStartOfDay();

        List<AccountOnlineReportProjection> projections = repository.getReportByDateRange(fromDateTime, toDateTime);

        return accountOnlineReportMapper.projectionsToResponses(projections);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAccountOpeningLog(String idNumber, OpenAccStatusEnum status, String stepInfo, Exception exception) {
        log.info("Creating account opening log - ID: {}, Status: {}, Step: {}", idNumber, status, stepInfo);

        StringBuilder remarkBuilder = new StringBuilder();
        remarkBuilder.append("Step: ").append(stepInfo);

        if (exception != null) {
            remarkBuilder.append(" | Error: ").append(exception.getClass().getSimpleName());
            remarkBuilder.append(" | Message: ").append(exception.getMessage());
        }

        AccountOnlineReportLog onlineReportLog = AccountOnlineReportLog.builder()
                .idNumber(idNumber)
                .status(status)
                .remark(remarkBuilder.toString())
                .build();

        // Alerting is now handled by OpenAccountServiceImpl to allow conditional routing (Internal vs Monitor)
        // and to avoid duplicates.
        // DO NOT call alertsOpenAccOnlineService here.

        repository.save(onlineReportLog);
    }


    @Override
    public List<AccountOnlineReportLogResponse> getAllLogs(AccountOnlineReportLogDto request) {
        log.info("Fetching all logs - From: {}, To: {}, Status: {}",
                request.getFromDate(), request.getToDate(), request.getStatus());

        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().plusDays(1).atStartOfDay() : null;
        List<OpenAccStatusEnum> statuses = request.getStatus() != null ? List.of(request.getStatus()) : null;

        List<AccountOnlineReportLog> logs = repository.findByDateRangeAndStatuses(fromDateTime, toDateTime, statuses);

        log.info("Found {} logs", logs.size());
        return accountOnlineReportMapper.toResponseList(logs);
    }

    @Override
    public PaginationResponse<AccountOnlineReportLogResponse> getLogsWithPagination(AccountOnlineReportLogDto request) {
        log.info("Fetching paginated logs - From: {}, To: {}, Status: {}, Page: {}, Size: {}",
                request.getFromDate(), request.getToDate(), request.getStatus(),
                request.getPageNo(), request.getPageSize());

        // Convert page number from 1-indexed to 0-indexed for Spring Data
        int pageIndex = request.getPageNo() - 1;

        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().plusDays(1).atStartOfDay() : null;
        List<OpenAccStatusEnum> statuses = request.getStatus() != null ? List.of(request.getStatus()) : null;

        Pageable pageable = PageRequest.of(pageIndex, request.getPageSize());

        Page<AccountOnlineReportLog> page = repository.findByDateRangeAndStatusesPaged(
                fromDateTime, toDateTime, statuses, pageable);

        List<AccountOnlineReportLogResponse> responseList = accountOnlineReportMapper.toResponseList(page.getContent());

        log.info("Found {} logs on page {} of {}", page.getNumberOfElements(),
                request.getPageNo(), page.getTotalPages());

        return new PaginationResponse<>(
                responseList,
                request.getPageNo(),
                request.getPageSize(),
                page.getTotalElements()
        );
    }

}
