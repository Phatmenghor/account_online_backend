package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchSessionResponseDto {
    private String batchSessionId;
    private LocalDate batchSessionDate;
    private LocalDateTime processedDate;
    private Long recordCount;
    private String processedBy;
}
