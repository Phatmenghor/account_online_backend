package com.internal.feature.report_staging.mapper;

import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.models.CbcFinalRecordEntity;
import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(
    componentModel = "spring", 
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CbcMapper {

    // ============ STAGING MAPPINGS ============
    
    @Mapping(target = "recordType", constant = "STAGING")
    @Mapping(target = "batchSessionId", ignore = true)
    @Mapping(target = "batchSessionDate", ignore = true)
    @Mapping(target = "processedDate", ignore = true)
    @Mapping(target = "originalLoadDate", ignore = true)
    @Mapping(target = "wasUpdated", ignore = true)
    @Mapping(target = "updateCount", ignore = true)
    @Mapping(target = "lastUpdatedDate", ignore = true)
    @Mapping(target = "archiveStatus", ignore = true)
    CbcRecordResponseDto stagingToResponseDto(CbcStagingRecordEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "isUpdated", constant = "true")
    @Mapping(target = "validationStatus", constant = "PENDING")
    void updateStagingFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcStagingRecordEntity entity);

    // ============ FINAL MAPPINGS ============
    
    @Mapping(target = "recordType", constant = "FINAL")
    @Mapping(target = "isUpdated", ignore = true)
    @Mapping(target = "validationStatus", ignore = true)
    @Mapping(target = "validationErrors", ignore = true)
    CbcRecordResponseDto finalToResponseDto(CbcFinalRecordEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "batchSessionId", ignore = true)
    @Mapping(target = "batchSessionDate", ignore = true)
    @Mapping(target = "processedDate", ignore = true)
    @Mapping(target = "originalLoadDate", ignore = true)
    @Mapping(target = "archiveStatus", ignore = true)
    @Mapping(target = "updateCount", expression = "java(entity.getUpdateCount() != null ? entity.getUpdateCount() + 1 : 1)")
    @Mapping(target = "lastUpdatedDate", expression = "java(java.time.LocalDateTime.now())")
    void updateFinalFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcFinalRecordEntity entity);

    // ============ STAGING TO FINAL CONVERSION ============
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "batchSessionId", ignore = true)
    @Mapping(target = "batchSessionDate", ignore = true)
    @Mapping(target = "processedDate", ignore = true)
    @Mapping(target = "originalLoadDate", ignore = true)
    @Mapping(target = "wasUpdated", source = "isUpdated")
    @Mapping(target = "updateCount", constant = "0")
    @Mapping(target = "lastUpdatedDate", ignore = true)
    @Mapping(target = "archiveStatus", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CbcFinalRecordEntity stagingToFinal(CbcStagingRecordEntity stagingEntity);

    // ============ LIST MAPPINGS ============
    
    List<CbcRecordResponseDto> stagingListToResponseDtoList(List<CbcStagingRecordEntity> entities);
    
    List<CbcRecordResponseDto> finalListToResponseDtoList(List<CbcFinalRecordEntity> entities);

    // ============ AFTER MAPPING CUSTOMIZATIONS ============

    @AfterMapping
    default void afterUpdateStaging(@MappingTarget CbcStagingRecordEntity target, 
                                  CbcUpdateRequestDto source) {
        target.setUpdatedAt(LocalDateTime.now());
        if (target.getIsUpdated() == null) {
            target.setIsUpdated(true);
        }
    }

    @AfterMapping
    default void afterUpdateFinal(@MappingTarget CbcFinalRecordEntity target, 
                                CbcUpdateRequestDto source) {
        target.setUpdatedAt(LocalDateTime.now());
        target.setLastUpdatedDate(LocalDateTime.now());
        if (target.getUpdateCount() == null) {
            target.setUpdateCount(1);
        } else {
            target.setUpdateCount(target.getUpdateCount() + 1);
        }
    }

    // ============ HELPER METHODS FOR MANUAL MAPPING ============
    
    /**
     * Manual mapping method to set batch session information
     */
    default void setBatchSessionInfo(CbcFinalRecordEntity target, 
                                   String batchSessionId,
                                   LocalDate batchSessionDate,
                                   LocalDateTime processedDate,
                                   LocalDate originalLoadDate,
                                   String createdBy) {
        target.setBatchSessionId(batchSessionId);
        target.setBatchSessionDate(batchSessionDate);
        target.setProcessedDate(processedDate);
        target.setOriginalLoadDate(originalLoadDate);
        target.setCreatedBy(createdBy);
        target.setUpdatedBy(createdBy);
    }
}