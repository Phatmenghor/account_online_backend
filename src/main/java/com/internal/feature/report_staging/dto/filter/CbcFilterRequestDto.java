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
    
    // Date filters (for batch sessions or final records)
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Batch session filter (for final records)
    private String batchSessionId;
    
    // Simple search
    private String search;
    
    // Record type filter
    private String recordType; // "STAGING" or "FINAL"
}