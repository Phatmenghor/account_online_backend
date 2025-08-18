package com.internal.feature.report_staging.service.impl;

import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.request.MoveToFinalRequestDto;
import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcBulkUpdateRequestDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.service.CbcDataService;
import com.internal.feature.report_staging.service.CbcFinalService;
import com.internal.feature.report_staging.service.CbcStagingService;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbcDataServiceImpl implements CbcDataService {

    private final CbcStagingService stagingService;
    private final CbcFinalService finalService;

    // ============ STAGING OPERATIONS (Delegated) ============

    @Override
    public DataLoadStatusDto loadCbcData(CbcDataRequestDto request) {
        log.info("Delegating CBC data loading to staging service");
        return stagingService.loadCbcData(request);
    }

    @Override
    public DataLoadStatusDto getLoadStatus() {
        return stagingService.getLoadStatus();
    }

    @Override
    public PaginationResponse<CbcRecordResponseDto> getStagingRecordsPaginated(CbcFilterRequestDto filterRequest) {
        log.debug("Delegating staging records pagination to staging service");
        return stagingService.getStagingRecordsPaginated(filterRequest);
    }

    @Override
    public List<CbcRecordResponseDto> getAllStagingRecords(CbcFilterRequestDto filterRequest) {
        log.debug("Delegating all staging records retrieval to staging service");
        return stagingService.getAllStagingRecords(filterRequest);
    }

    @Override
    public CbcRecordResponseDto getStagingRecordById(UUID id) {
        log.debug("Delegating staging record retrieval by ID to staging service");
        return stagingService.getStagingRecordById(id);
    }

    @Override
    public CbcRecordResponseDto updateStagingRecord(UUID id, CbcUpdateRequestDto updateRequest) {
        log.info("Delegating staging record update to staging service");
        return stagingService.updateStagingRecord(id, updateRequest);
    }

    @Override
    public List<CbcRecordResponseDto> updateMultipleStagingRecords(CbcBulkUpdateRequestDto bulkUpdateRequest) {
        log.info("Delegating bulk staging records update to staging service");
        return stagingService.updateMultipleStagingRecords(bulkUpdateRequest);
    }

    @Override
    public long getStagingRecordsCount() {
        return stagingService.getStagingRecordsCount();
    }

    @Override
    public void clearStagingDataSync() {
        log.info("Delegating staging data clearing to staging service");
        stagingService.clearStagingDataSync();
    }

    // ============ FINAL OPERATIONS (Delegated) ============

    @Override
    public String moveStagingToFinal(MoveToFinalRequestDto request) {
        log.info("Delegating staging to final move operation to final service");
        return finalService.moveStagingToFinal(request);
    }

    @Override
    public PaginationResponse<CbcRecordResponseDto> getFinalRecordsPaginated(CbcFilterRequestDto filterRequest) {
        log.debug("Delegating final records pagination to final service");
        return finalService.getFinalRecordsPaginated(filterRequest);
    }

    @Override
    public List<CbcRecordResponseDto> getAllFinalRecords(CbcFilterRequestDto filterRequest) {
        log.debug("Delegating all final records retrieval to final service");
        return finalService.getAllFinalRecords(filterRequest);
    }

    @Override
    public List<BatchSessionResponseDto> getAllBatchSessions() {
        log.debug("Delegating batch sessions retrieval to final service");
        return finalService.getAllBatchSessions();
    }

    @Override
    public List<BatchSessionResponseDto> getBatchSessionsWithFilter(CbcFilterRequestDto filterRequest) {
        log.debug("Delegating filtered batch sessions retrieval to final service");
        return finalService.getBatchSessionsWithFilter(filterRequest);
    }

    @Override
    public CbcRecordResponseDto getFinalRecordById(UUID id) {
        log.debug("Delegating final record retrieval by ID to final service");
        return finalService.getFinalRecordById(id);
    }

    @Override
    public CbcRecordResponseDto updateFinalRecord(UUID id, CbcUpdateRequestDto updateRequest) {
        log.info("Delegating final record update to final service");
        return finalService.updateFinalRecord(id, updateRequest);
    }

    @Override
    public List<CbcRecordResponseDto> updateMultipleFinalRecords(CbcBulkUpdateRequestDto bulkUpdateRequest) {
        log.info("Delegating bulk final records update to final service");
        return finalService.updateMultipleFinalRecords(bulkUpdateRequest);
    }

    @Override
    public long getFinalRecordsCount() {
        return finalService.getFinalRecordsCount();
    }

    // ============ CROSS-CUTTING OPERATIONS ============

    @Override
    public void validateDataIntegrity() {
        log.info("Starting data integrity validation");
        
        // Validate staging data
        long stagingCount = stagingService.getStagingRecordsCount();
        long stagingUpdatedCount = stagingService.getUpdatedRecordsCount();
        
        // Validate final data
        long finalCount = finalService.getFinalRecordsCount();
        long finalActiveCount = finalService.getActiveFinalRecordsCount();
        long finalUpdatedCount = finalService.getUpdatedRecordsCount();
        
        log.info("Data Integrity Report:");
        log.info("Staging Records: {} total, {} updated", stagingCount, stagingUpdatedCount);
        log.info("Final Records: {} total, {} active, {} updated", finalCount, finalActiveCount, finalUpdatedCount);
        
        // Add more validation logic as needed
        if (stagingCount > 0 && finalCount == 0) {
            log.warn("Warning: Staging has data but final table is empty");
        }
        
        if (finalCount > 0 && finalActiveCount == 0) {
            log.warn("Warning: Final table has data but no active records");
        }
        
        log.info("Data integrity validation completed");
    }

    @Override
    public void generateDataReport() {
        log.info("Generating comprehensive data report");
        
        // Generate staging statistics
        long stagingTotal = stagingService.getStagingRecordsCount();
        long stagingUpdated = stagingService.getUpdatedRecordsCount();
        long stagingPending = stagingService.getValidationSummary("PENDING");
        long stagingValidated = stagingService.getValidationSummary("VALIDATED");
        long stagingInvalid = stagingService.getValidationSummary("INVALID");
        
        // Generate final statistics  
        long finalTotal = finalService.getFinalRecordsCount();
        long finalActive = finalService.getActiveFinalRecordsCount();
        long finalUpdated = finalService.getUpdatedRecordsCount();
        
        // Get recent batch sessions
        List<BatchSessionResponseDto> recentBatches = finalService.getAllBatchSessions();
        
        log.info("=== CBC DATA REPORT ===");
        log.info("STAGING STATISTICS:");
        log.info("  Total Records: {}", stagingTotal);
        log.info("  Updated Records: {}", stagingUpdated);
        log.info("  Pending Validation: {}", stagingPending);
        log.info("  Validated Records: {}", stagingValidated);
        log.info("  Invalid Records: {}", stagingInvalid);
        
        log.info("FINAL STATISTICS:");
        log.info("  Total Records: {}", finalTotal);
        log.info("  Active Records: {}", finalActive);
        log.info("  Updated Records: {}", finalUpdated);
        
        log.info("BATCH SESSIONS:");
        log.info("  Total Batch Sessions: {}", recentBatches.size());
        
        if (!recentBatches.isEmpty()) {
            BatchSessionResponseDto latest = recentBatches.get(0);
            log.info("  Latest Batch: {} ({} records, processed on {})", 
                    latest.getBatchSessionId(), 
                    latest.getRecordCount(), 
                    latest.getProcessedDate());
        }
        
        log.info("=== END REPORT ===");
    }
}