package com.internal.feature.logs_report.controller;

import com.internal.feature.logs_report.dob.request.FilterNidValidationLogsDto;
import com.internal.feature.logs_report.service.serviceImpl.NidValidationExcelServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/report/nid-validation")
@RequiredArgsConstructor
public class NidValidationReportController {

    private final NidValidationExcelServiceImpl excelService;

    @PostMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody FilterNidValidationLogsDto filterDto) {
        try {
            byte[] excelData = excelService.generateExcel(filterDto);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=nid-validation-report.xlsx")
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
