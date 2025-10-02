package com.internal.feature.report_trainee.dto.update;

import com.internal.enumation.ApplicationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportTraineeUpdateDto {
    private String reportRemark;
    private String challenge;
    private String recommend;
}
