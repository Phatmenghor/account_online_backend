package com.internal.feature.report_staging.mapper;

import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.models.CbcFinalRecordEntity;
import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CbcMapper {

    // ============ STAGING MAPPINGS ============
    
    @Mapping(target = "recordType", constant = "STAGING")
    @Mapping(target = "batchSessionId", ignore = true)
    @Mapping(target = "batchSessionDate", ignore = true)
    @Mapping(target = "processedDate", ignore = true)
    @Mapping(target = "wasUpdated", ignore = true)
    CbcRecordResponseDto stagingToResponseDto(CbcStagingRecordEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "isUpdated", constant = "true")
    void updateStagingFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcStagingRecordEntity entity);

    // ============ FINAL MAPPINGS ============
    
    @Mapping(target = "recordType", constant = "FINAL")
    @Mapping(target = "isUpdated", ignore = true)
    CbcRecordResponseDto finalToResponseDto(CbcFinalRecordEntity entity);

    // ============ STAGING TO FINAL CONVERSION ============
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "batchSessionId", source = "batchSessionId")
    @Mapping(target = "batchSessionDate", source = "batchSessionDate")
    @Mapping(target = "processedDate", source = "processedDate")
    @Mapping(target = "originalLoadDate", source = "originalLoadDate")
    @Mapping(target = "wasUpdated", source = "stagingEntity.isUpdated")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CbcFinalRecordEntity stagingToFinal(CbcStagingRecordEntity stagingEntity,
                                        String batchSessionId,
                                        LocalDate batchSessionDate,
                                        LocalDateTime processedDate,
                                        LocalDate originalLoadDate);

    // ============ BATCH SESSION MAPPINGS ============
    
    default BatchSessionResponseDto mapToBatchSessionDto(Object[] result) {
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