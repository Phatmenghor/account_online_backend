package com.internal.feature.project.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSearchRequestDto {
    private String search;
    private int pageNo = 1;
    private int pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";
}