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
    @Mapping(target = "batchSessionId", source = "batchSessionId")
    @Mapping(target = "batchSessionDate", source = "batchSessionDate")
    @Mapping(target = "processedDate", source = "processedDate")
    @Mapping(target = "originalLoadDate", source = "originalLoadDate")
    @Mapping(target = "wasUpdated", source = "stagingEntity.isUpdated")
    @Mapping(target = "updateCount", constant = "0")
    @Mapping(target = "lastUpdatedDate", ignore = true)
    @Mapping(target = "archiveStatus", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CbcFinalRecordEntity stagingToFinal(
        CbcStagingRecordEntity stagingEntity,
        String batchSessionId,
        LocalDate batchSessionDate,
        LocalDateTime processedDate,
        LocalDate originalLoadDate
    );

    // ============ LIST MAPPINGS ============
    
    List<CbcRecordResponseDto> stagingListToResponseDtoList(List<CbcStagingRecordEntity> entities);
    
    List<CbcRecordResponseDto> finalListToResponseDtoList(List<CbcFinalRecordEntity> entities);

    // ============ AFTER MAPPING CUSTOMIZATIONS ============

    @AfterMapping
    default void afterStagingToFinal(@MappingTarget CbcFinalRecordEntity target, 
                                   CbcStagingRecordEntity source, 
                                   @Context BatchConversionContext context) {
        if (context != null) {
            target.setBatchSessionId(context.getBatchSessionId());
            target.setBatchSessionDate(context.getBatchSessionDate());
            target.setProcessedDate(context.getProcessedDate());
            target.setOriginalLoadDate(context.getOriginalLoadDate());
            target.setCreatedBy(context.getCreatedBy());
            target.setUpdatedBy(context.getCreatedBy());
        }
    }

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

    // ============ CONTEXT FOR BATCH OPERATIONS ============
    
    @Context
    class BatchConversionContext {
        private String batchSessionId;
        private LocalDate batchSessionDate;
        private LocalDateTime processedDate;
        private LocalDate originalLoadDate;
        private String createdBy;

        public BatchConversionContext(String batchSessionId, LocalDate batchSessionDate, 
                                    LocalDateTime processedDate, LocalDate originalLoadDate, String createdBy) {
            this.batchSessionId = batchSessionId;
            this.batchSessionDate = batchSessionDate;
            this.processedDate = processedDate;
            this.originalLoadDate = originalLoadDate;
            this.createdBy = createdBy;
        }

        // Getters
        public String getBatchSessionId() { return batchSessionId; }
        public LocalDate getBatchSessionDate() { return batchSessionDate; }
        public LocalDateTime getProcessedDate() { return processedDate; }
        public LocalDate getOriginalLoadDate() { return originalLoadDate; }
        public String getCreatedBy() { return createdBy; }
    }
}