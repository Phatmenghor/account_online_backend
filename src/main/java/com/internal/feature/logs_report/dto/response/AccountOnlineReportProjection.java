package com.internal.feature.logs_report.dto.response;

import com.internal.enumation.OpenAccStatusEnum;

import java.time.LocalDate;

public interface AccountOnlineReportProjection {
    LocalDate getDate();
    OpenAccStatusEnum getStatus();
    Long getCount();
}
