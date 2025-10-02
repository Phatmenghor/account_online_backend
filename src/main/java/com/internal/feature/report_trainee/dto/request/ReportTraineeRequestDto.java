package com.internal.feature.report_trainee.dto.request;

import com.internal.enumation.ApplicationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportTraineeRequestDto {
    private String reportRemark;
    private String challenge;
    private String recommend;
}