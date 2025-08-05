package com.account_sell.feature.auth.controllers;

import com.account_sell.enumation.CamdxLogEnum;
import com.account_sell.exceptions.response.ApiResponse;
import com.account_sell.feature.auth.dto.response.LogRegisterAppDto;
import com.account_sell.feature.auth.dto.response.ResponseLogReport;
import com.account_sell.feature.auth.dto.response.ResponseLogReportExport;
import com.account_sell.feature.auth.dto.response.TotalCountDto;
import com.account_sell.feature.auth.service.LogReportService;
import com.account_sell.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/camdx-logs")
@RequiredArgsConstructor
public class LogReportController {

    private final LogReportService camdxLogService;

    @PostMapping("/report")
    public ApiResponse<PaginationResponse<ResponseLogReport>> getLogReport(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam CamdxLogEnum status,
            @RequestParam(required = false) String appName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return new ApiResponse<>(
                "success",
                "Get all data log report successfully",
                camdxLogService.getReport(pageNo, pageSize, status, appName, startDate, endDate)
        );
    }


    @PostMapping("/report-excel")
    public ApiResponse<ResponseLogReportExport> getLogReportExcel(
            @RequestParam CamdxLogEnum status,
            @RequestParam(required = false) String appName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return new ApiResponse<>(
                "success",
                "et all data log report excel successfully",
                camdxLogService.getReportExcel(status, appName, startDate, endDate)
        );
    }

    @PostMapping("/report-excel/count")
    public ApiResponse<List<TotalCountDto>> getLogReportExcelCount(
            @RequestParam CamdxLogEnum status,
            @RequestParam(required = false) String appName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return new ApiResponse<>(
                "success",
                "Count data that export to excel successfully",
                camdxLogService.getCountReportExcel(status, appName, startDate, endDate)
        );
    }

    // Get list app name from camdx register
    @PostMapping("/app-names")
    public ApiResponse<List<LogRegisterAppDto>> getAppNames() {
        return new ApiResponse<>(
                "success",
                "Get data camdx register name app successfully.",
                camdxLogService.getAppNames()
        );
    }

}
