package com.internal.feature.logs_report.controller;

import com.internal.feature.logs_report.dto.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportResponse;
import com.internal.feature.logs_report.repository.AccountOnlineReportLogRepository;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.logs_report.service.serviceImpl.AccountOnlineReportLogImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/report/account-online-report")
@RequiredArgsConstructor
public class AccountOnlineReportLogController {

    private final AccountOnlineReportLogImpl excelService;
    private final AccountOnlineReportLogService reportLogService;

    @PostMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody AccountOnlineReportLogDto filterDto) {
        try {
            byte[] excelData = excelService.generateExcel(filterDto);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=nid-validation-report.xlsx")
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping()
    public ResponseEntity<List<AccountOnlineReportResponse>> getAccountOnlineReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        List<AccountOnlineReportResponse> report = reportLogService.getReportByDateRange(fromDate, toDate);
        return ResponseEntity.ok(report);
    }
}
