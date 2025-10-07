package com.internal.feature.report_trainee.dto.filter;

import com.internal.enumation.ApplicationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllReportTraineeRequestDto {
    private String search;
    private int pageNo = 1;
    private int pageSize = 10;
}