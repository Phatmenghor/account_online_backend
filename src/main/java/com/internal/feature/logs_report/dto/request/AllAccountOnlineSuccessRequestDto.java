package com.internal.feature.logs_report.dto.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AllAccountOnlineSuccessRequestDto {
    @Builder.Default
    private int pageNo = 1;

    @Builder.Default
    private int pageSize = 10;

    private String search;
}
