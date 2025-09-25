package com.internal.feature.project.dto.filter;

import com.internal.enumation.ProjectStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllProjectRequestDto {
    private String search;
    private int pageNo = 1;
    private int pageSize = 10;

    private ProjectStatusEnum projectStatus;
}