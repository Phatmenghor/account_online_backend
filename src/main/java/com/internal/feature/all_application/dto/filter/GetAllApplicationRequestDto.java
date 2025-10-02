package com.internal.feature.all_application.dto.filter;

import com.internal.enumation.ApplicationStatusEnum;
import com.internal.enumation.ProjectStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllApplicationRequestDto {
    private String search;
    private int pageNo = 1;
    private int pageSize = 10;

    private ApplicationStatusEnum applicationStatus;
}