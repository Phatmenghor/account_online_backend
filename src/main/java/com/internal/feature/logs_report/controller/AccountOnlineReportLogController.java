package com.internal.feature.logs_report.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.logs_report.dto.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportLogResponse;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportResponse;
import com.internal.feature.logs_report.repository.AccountOnlineReportLogRepository;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.logs_report.service.serviceImpl.AccountOnlineReportLogImpl;
import com.internal.utils.constants.ResponseMessage;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/report/account-online-report")
@RequiredArgsConstructor
@Slf4j
public class AccountOnlineReportLogController {

    private final AccountOnlineReportLogService reportLogService;

    @PostMapping()
    public ResponseEntity<List<AccountOnlineReportResponse>> getAccountOnlineReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        List<AccountOnlineReportResponse> report = reportLogService.getReportByDateRange(fromDate, toDate);
        return ResponseEntity.ok(report);
    }


    @PostMapping("/all-data")
    public ResponseEntity<ApiResponse<List<AccountOnlineReportLogResponse>>> getAllLogs(
            @Valid @RequestBody AccountOnlineReportLogDto request) {

        log.info("Fetching all account online report logs - From: {}, To: {}, Status: {}",
                request.getFromDate(), request.getToDate(), request.getStatus());

        List<AccountOnlineReportLogResponse> logs = reportLogService.getAllLogs(request);

        log.info("Successfully retrieved {} account online report logs", logs.size());

        return ResponseEntity.ok(ApiResponse.success(
                ResponseMessage.REPORT_LOGS_RETRIEVED,
                logs
        ));
    }

    @PostMapping("/all-view")
    public ResponseEntity<ApiResponse<PaginationResponse<AccountOnlineReportLogResponse>>> getLogsWithPagination(
            @Valid @RequestBody AccountOnlineReportLogDto request) {

        log.info("Fetching paginated account online report logs - From: {}, To: {}, Status: {}, Page: {}, Size: {}",
                request.getFromDate(), request.getToDate(), request.getStatus(),
                request.getPageNo(), request.getPageSize());

        PaginationResponse<AccountOnlineReportLogResponse> pagedResponse =
                reportLogService.getLogsWithPagination(request);

        log.info("Successfully retrieved paginated account online report logs - Page: {} of {}, Total: {}",
                pagedResponse.getPageNo(), pagedResponse.getTotalPages(), pagedResponse.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(
                ResponseMessage.REPORT_LOGS_RETRIEVED,
                pagedResponse
        ));
    }
}
