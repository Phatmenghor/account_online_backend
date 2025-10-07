package com.internal.feature.all_application.dto.request;

import com.internal.enumation.ApplicationStatusEnum;
import com.internal.enumation.ProjectStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequestDto {
    private String projectName;
    private String memberInvolved;
    private String remark;
    private String department;
    private String year;
    private String urlLink;
    private ApplicationStatusEnum applicationStatus;
}