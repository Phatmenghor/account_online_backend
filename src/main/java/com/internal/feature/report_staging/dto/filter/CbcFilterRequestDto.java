package com.internal.feature.report_staging.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcFilterRequestDto {
    
    // Pagination - starts from 1
    private Integer page = 1;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    
    // Date filters
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Simple search
    private String search;
}