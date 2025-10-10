package com.internal.feature.report_trainee.dto.resposne;

import com.internal.enumation.ApplicationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportTraineeResponseDto {
    private Long id;
    private String reportBy;
    private String reportRemark;
    private String challenge;
    private String recommend;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
