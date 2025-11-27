package com.internal.feature.logs_report.dto.response;

import com.internal.enumation.OpenAccStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountOnlineReportLogResponse {

    private UUID id;
    private String idNumber;
    private OpenAccStatusEnum status;
    private String remark;
    private LocalDateTime createdAt;
}
