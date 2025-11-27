package com.internal.feature.logs_report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineReportResponse {
    private LocalDate date;
    private Long successCount;
    private Long failureCount;
    private Long amlCount;
}
