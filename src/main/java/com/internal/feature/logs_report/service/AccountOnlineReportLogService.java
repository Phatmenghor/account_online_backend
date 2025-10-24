package com.internal.feature.logs_report.service;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.logs_report.dob.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.model.AccountOnlineReportLog;

public interface AccountOnlineReportLogService {
    AccountOnlineReportLog saveLogReport(String idNumber, OpenAccStatusEnum status, String remark);
    byte[] generateExcel(AccountOnlineReportLogDto filterDto) throws Exception;
}
