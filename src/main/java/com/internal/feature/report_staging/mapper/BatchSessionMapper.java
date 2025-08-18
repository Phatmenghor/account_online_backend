
package com.internal.feature.report_staging.mapper;

import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class BatchSessionMapper {

    public BatchSessionResponseDto mapToBatchSessionDto(Object[] result) {
        if (result == null || result.length < 5) {
            return null;
        }
        
        BatchSessionResponseDto dto = new BatchSessionResponseDto();
        dto.setBatchSessionId((String) result[0]);
        dto.setBatchSessionDate((LocalDate) result[1]);
        dto.setProcessedDate((LocalDateTime) result[2]);
        dto.setRecordCount(((Number) result[3]).longValue());
        dto.setProcessedBy((String) result[4]);
        
        return dto;
    }
}