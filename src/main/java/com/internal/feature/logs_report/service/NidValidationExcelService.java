package com.internal.feature.logs_report.service;

import com.internal.feature.logs_report.dob.request.FilterNidValidationLogsDto;

public interface NidValidationExcelService {
    byte[] generateExcel(FilterNidValidationLogsDto filterDto) throws Exception;
}
