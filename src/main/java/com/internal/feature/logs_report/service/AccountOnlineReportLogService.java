package com.internal.feature.logs_report.service;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.logs_report.dto.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportResponse;

import java.time.LocalDate;
import java.util.List;

public interface AccountOnlineReportLogService {
    void saveLogReport(String idNumber, OpenAccStatusEnum status, String remark);
    void createAccountOpeningLog(String idNumber, OpenAccStatusEnum status, String stepInfo, Exception exception);
    byte[] generateExcel(AccountOnlineReportLogDto filterDto) throws Exception;
    public List<AccountOnlineReportResponse> getReportByDateRange(LocalDate fromDate, LocalDate toDate);
}
