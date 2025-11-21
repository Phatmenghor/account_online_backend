package com.internal.feature.logs_report.service;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.logs_report.dto.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportLogResponse;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportResponse;
import com.internal.utils.pagination.PaginationResponse;

import java.time.LocalDate;
import java.util.List;

public interface AccountOnlineReportLogService {
    void saveLogReport(String idNumber, OpenAccStatusEnum status, String remark);
    void createAccountOpeningLog(String idNumber, OpenAccStatusEnum status, String stepInfo, Exception exception);
    public List<AccountOnlineReportResponse> getReportByDateRange(LocalDate fromDate, LocalDate toDate);
    List<AccountOnlineReportLogResponse> getAllLogs(AccountOnlineReportLogDto request);

    PaginationResponse<AccountOnlineReportLogResponse> getLogsWithPagination(AccountOnlineReportLogDto request);
}
