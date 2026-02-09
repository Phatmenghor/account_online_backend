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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AccountOnlineReportLogImpl implements AccountOnlineReportLogService {

    private final AccountOnlineReportLogRepository repository;
    private final AccountOnlineReportMapper accountOnlineReportMapper;

    @Override
    public void saveLogReport(String idNumber, OpenAccStatusEnum status, String remark) {
        log.info("========== START saveLogReport ==========");
        log.info("Request - idNumber: {}, status: {}, remark: {}", idNumber, status, remark);
        repository.save(AccountOnlineReportLog.builder()
                .idNumber(idNumber)
                .status(status)
                .remark(remark)
                .build());
        log.info("Response - saved successfully for idNumber: {}", idNumber);
        log.info("========== END saveLogReport ==========");
    }

    @Override
    public List<AccountOnlineReportResponse> getReportByDateRange(LocalDate fromDate, LocalDate toDate) {
        log.info("========== START getReportByDateRange ==========");
        log.info("Request - fromDate: {}, toDate: {}", fromDate, toDate);
        List<AccountOnlineReportProjection> projections = repository.getReportByDateRange(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay());
        List<AccountOnlineReportResponse> responses = accountOnlineReportMapper.projectionsToResponses(projections);
        log.info("Response - totalRecords: {}", responses.size());
        log.info("========== END getReportByDateRange ==========");
        return responses;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAccountOpeningLog(String idNumber, OpenAccStatusEnum status, String stepInfo, Exception exception) {
        log.info("========== START createAccountOpeningLog ==========");
        log.info("Request - idNumber: {}, status: {}, stepInfo: {}, hasException: {}", idNumber, status, stepInfo, exception != null);
        StringBuilder remark = new StringBuilder("Step: ").append(stepInfo);
        if (exception != null) {
            remark.append(" | Error: ").append(exception.getClass().getSimpleName())
                    .append(" | Message: ").append(exception.getMessage());
            log.error("Exception detail - type: {}, message: {}", exception.getClass().getSimpleName(), exception.getMessage());
        }
        repository.save(AccountOnlineReportLog.builder()
                .idNumber(idNumber)
                .status(status)
                .remark(remark.toString())
                .build());
        log.info("Response - saved successfully for idNumber: {}, remark: {}", idNumber, remark);
        log.info("========== END createAccountOpeningLog ==========");
    }

    @Override
    public List<AccountOnlineReportLogResponse> getAllLogs(AccountOnlineReportLogDto request) {
        log.info("========== START getAllLogs ==========");
        log.info("Request - fromDate: {}, toDate: {}, status: {}", request.getFromDate(), request.getToDate(), request.getStatus());
        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().plusDays(1).atStartOfDay() : null;

        List<AccountOnlineReportLog> logs = repository.findByDateRangeAndStatuses(fromDateTime, toDateTime, request.getStatus());
        List<AccountOnlineReportLogResponse> responses = accountOnlineReportMapper.toResponseList(logs);
        log.info("Response - totalRecords: {}", responses.size());
        log.info("========== END getAllLogs ==========");
        return responses;
    }

    @Override
    public PaginationResponse<AccountOnlineReportLogResponse> getLogsWithPagination(AccountOnlineReportLogDto request) {
        log.info("========== START getLogsWithPagination ==========");
        log.info("Request - fromDate: {}, toDate: {}, status: {}, pageNo: {}, pageSize: {}",
                request.getFromDate(), request.getToDate(), request.getStatus(), request.getPageNo(), request.getPageSize());
        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().plusDays(1).atStartOfDay() : null;

        Page<AccountOnlineReportLog> page = repository.findByDateRangeAndStatusesPaged(
                fromDateTime, toDateTime, request.getStatus(),
                PageRequest.of(request.getPageNo() - 1, request.getPageSize()));

        PaginationResponse<AccountOnlineReportLogResponse> response = new PaginationResponse<>(
                accountOnlineReportMapper.toResponseList(page.getContent()),
                request.getPageNo(),
                request.getPageSize(),
                page.getTotalElements());
        log.info("Response - totalRecords: {}, totalPages: {}, currentPage: {}, pageSize: {}",
                page.getTotalElements(), page.getTotalPages(), request.getPageNo(), request.getPageSize());
        log.info("========== END getLogsWithPagination ==========");
        return response;
    }
}
