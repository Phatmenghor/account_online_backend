
package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataLoadStatusDto {
    private String status; // LOADING, SUCCESS, FAILED, IDLE
    private String message;
    private Long totalRecords;
    private LocalDateTime lastLoadTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}