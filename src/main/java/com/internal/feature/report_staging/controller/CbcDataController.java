package com.internal.feature.report_staging.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.request.MoveToFinalRequestDto;
import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcBulkUpdateRequestDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.service.CbcDataService;
import com.internal.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cbc-data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CBC Data Management")
public class CbcDataController {

    private final CbcDataService cbcDataService;

    // ============ DATA LOADING OPERATIONS ============

    @PostMapping("/staging/load")
    @Operation(summary = "Load CBC data from SQL Server to staging table")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<DataLoadStatusDto>> loadCbcData(@Valid @RequestBody CbcDataRequestDto request) {
        log.info("Received request to load CBC data for date range: {} to {}",
                request.getStartDate(), request.getEndDate());

        DataLoadStatusDto status = cbcDataService.loadCbcData(request);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data loading initiated successfully",
                status
        ));
    }

    @GetMapping("/staging/load-status")
    @Operation(summary = "Get current data loading status")
    public ResponseEntity<ApiResponse<DataLoadStatusDto>> getLoadStatus() {
        log.info("Fetching current load status");

        DataLoadStatusDto status = cbcDataService.getLoadStatus();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Load status retrieved successfully",
                status
        ));
    }

    @DeleteMapping("/staging/clear")
    @Operation(summary = "Clear all staging data")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<String>> clearStagingData() {
        log.info("Clearing all staging data");

        cbcDataService.clearStagingDataSync();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Staging data cleared successfully",
                "All staging records deleted"
        ));
    }

    // ============ STAGING RECORD OPERATIONS ============

    @PostMapping("/staging/records")
    @Operation(summary = "Get staging records with pagination and filtering")
    public ResponseEntity<ApiResponse<PaginationResponse<CbcRecordResponseDto>>> getStagingRecordsPaginated(
            @Valid @RequestBody CbcFilterRequestDto filterRequest) {
        log.info("Fetching staging records with pagination - Page: {}, Size: {}",
                filterRequest.getPage(), filterRequest.getSize());

        PaginationResponse<CbcRecordResponseDto> paginatedRecords = 
                cbcDataService.getStagingRecordsPaginated(filterRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("Staging records retrieved successfully. Page %d/%d", 
                        paginatedRecords.getPageNo(), paginatedRecords.getTotalPages()),
                paginatedRecords
        ));
    }

    @PostMapping("/staging/records/all")
    @Operation(summary = "Get all staging records without pagination")
    public ResponseEntity<ApiResponse<List<CbcRecordResponseDto>>> getAllStagingRecords(
            @Valid @RequestBody CbcFilterRequestDto filterRequest) {
        log.info("Fetching all staging records without pagination");

        List<CbcRecordResponseDto> records = cbcDataService.getAllStagingRecords(filterRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("Retrieved %d staging records", records.size()),
                records
        ));
    }

    @GetMapping("/staging/records/{id}")
    @Operation(summary = "Get single staging record by ID")
    public ResponseEntity<ApiResponse<CbcRecordResponseDto>> getStagingRecordById(@PathVariable UUID id) {
        log.info("Fetching staging record by ID: {}", id);

        CbcRecordResponseDto record = cbcDataService.getStagingRecordById(id);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Staging record retrieved successfully",
                record
        ));
    }

    @PutMapping("/staging/records/{id}")
    @Operation(summary = "Update single staging record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<CbcRecordResponseDto>> updateStagingRecord(
            @PathVariable UUID id,
            @Valid @RequestBody CbcUpdateRequestDto updateRequest) {
        log.info("Updating staging record with ID: {}", id);

        CbcRecordResponseDto updatedRecord = cbcDataService.updateStagingRecord(id, updateRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Staging record updated successfully",
                updatedRecord
        ));
    }

    @PutMapping("/staging/records/bulk")
    @Operation(summary = "Update multiple staging records in bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<List<CbcRecordResponseDto>>> updateMultipleStagingRecords(
            @Valid @RequestBody CbcBulkUpdateRequestDto bulkUpdateRequest) {
        log.info("Bulk updating {} staging records", bulkUpdateRequest.getUpdates().size());

        List<CbcRecordResponseDto> updatedRecords = cbcDataService.updateMultipleStagingRecords(bulkUpdateRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("Successfully updated %d staging records", updatedRecords.size()),
                updatedRecords
        ));
    }

    @GetMapping("/staging/count")
    @Operation(summary = "Get total count of staging records")
    public ResponseEntity<ApiResponse<RecordCountResponse>> getStagingRecordsCount() {
        log.info("Fetching staging records count");

        long count = cbcDataService.getStagingRecordsCount();
        RecordCountResponse countResponse = new RecordCountResponse(count, "STAGING");

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Staging records count retrieved successfully",
                countResponse
        ));
    }

    // ============ FINAL RECORD OPERATIONS ============

    @PostMapping("/staging/move-to-final")
    @Operation(summary = "Move all staging records to final table and create batch session")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<BatchSessionMoveResponse>> moveStagingToFinal(
            @RequestBody(required = false) MoveToFinalRequestDto request) {
        log.info("Moving staging records to final table");

        String batchSessionId = cbcDataService.moveStagingToFinal(request);
        
        BatchSessionMoveResponse response = new BatchSessionMoveResponse();
        response.setBatchSessionId(batchSessionId);
        response.setMessage("Records moved to final table successfully");

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Records moved to final table successfully with batch session: " + batchSessionId,
                response
        ));
    }

    @PostMapping("/final/records")
    @Operation(summary = "Get final records with pagination (filtered by batch session)")
    public ResponseEntity<ApiResponse<PaginationResponse<CbcRecordResponseDto>>> getFinalRecordsPaginated(
            @Valid @RequestBody CbcFilterRequestDto filterRequest) {
        log.info("Fetching final records with pagination - Page: {}, Size: {}, BatchSession: {}",
                filterRequest.getPage(), filterRequest.getSize(), filterRequest.getBatchSessionId());

        PaginationResponse<CbcRecordResponseDto> paginatedRecords = 
                cbcDataService.getFinalRecordsPaginated(filterRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("Final records retrieved successfully. Page %d/%d", 
                        paginatedRecords.getPageNo(), paginatedRecords.getTotalPages()),
                paginatedRecords
        ));
    }

    @PostMapping("/final/records/all")
    @Operation(summary = "Get all final records without pagination")
    public ResponseEntity<ApiResponse<List<CbcRecordResponseDto>>> getAllFinalRecords(
            @Valid @RequestBody CbcFilterRequestDto filterRequest) {
        log.info("Fetching all final records without pagination");

        List<CbcRecordResponseDto> records = cbcDataService.getAllFinalRecords(filterRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("Retrieved %d final records", records.size()),
                records
        ));
    }

    @GetMapping("/final/records/{id}")
    @Operation(summary = "Get single final record by ID")
    public ResponseEntity<ApiResponse<CbcRecordResponseDto>> getFinalRecordById(@PathVariable UUID id) {
        log.info("Fetching final record by ID: {}", id);

        CbcRecordResponseDto record = cbcDataService.getFinalRecordById(id);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Final record retrieved successfully",
                record
        ));
    }

    @PutMapping("/final/records/{id}")
    @Operation(summary = "Update single final record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<CbcRecordResponseDto>> updateFinalRecord(
            @PathVariable UUID id, 
            @Valid @RequestBody CbcUpdateRequestDto updateRequest) {
        log.info("Updating final record with ID: {}", id);

        CbcRecordResponseDto updatedRecord = cbcDataService.updateFinalRecord(id, updateRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Final record updated successfully",
                updatedRecord
        ));
    }

    @PutMapping("/final/records/bulk")
    @Operation(summary = "Update multiple final records in bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<List<CbcRecordResponseDto>>> updateMultipleFinalRecords(
            @Valid @RequestBody CbcBulkUpdateRequestDto bulkUpdateRequest) {
        log.info("Bulk updating {} final records", bulkUpdateRequest.getUpdates().size());

        List<CbcRecordResponseDto> updatedRecords = cbcDataService.updateMultipleFinalRecords(bulkUpdateRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("Successfully updated %d final records", updatedRecords.size()),
                updatedRecords
        ));
    }

    @GetMapping("/final/count")
    @Operation(summary = "Get total count of final records")
    public ResponseEntity<ApiResponse<RecordCountResponse>> getFinalRecordsCount() {
        log.info("Fetching final records count");

        long count = cbcDataService.getFinalRecordsCount();
        RecordCountResponse countResponse = new RecordCountResponse(count, "FINAL");

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Final records count retrieved successfully",
                countResponse
        ));
    }

    // ============ BATCH SESSION OPERATIONS ============

    @GetMapping("/batch-sessions")
    @Operation(summary = "Get all batch sessions (for listing view)")
    public ResponseEntity<ApiResponse<List<BatchSessionResponseDto>>> getAllBatchSessions() {
        log.info("Fetching all batch sessions");

        List<BatchSessionResponseDto> batchSessions = cbcDataService.getAllBatchSessions();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("Retrieved %d batch sessions", batchSessions.size()),
                batchSessions
        ));
    }

    @PostMapping("/batch-sessions/filter")
    @Operation(summary = "Get batch sessions with date/search filters")
    public ResponseEntity<ApiResponse<List<BatchSessionResponseDto>>> getBatchSessionsWithFilter(
            @Valid @RequestBody CbcFilterRequestDto filterRequest) {
        log.info("Fetching batch sessions with filters");

        List<BatchSessionResponseDto> batchSessions = cbcDataService.getBatchSessionsWithFilter(filterRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("Retrieved %d filtered batch sessions", batchSessions.size()),
                batchSessions
        ));
    }

    // ============ UTILITY OPERATIONS ============

    @PostMapping("/validate-integrity")
    @Operation(summary = "Validate data integrity across staging and final tables")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<String>> validateDataIntegrity() {
        log.info("Starting data integrity validation");

        cbcDataService.validateDataIntegrity();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data integrity validation completed successfully",
                "Check logs for detailed integrity report"
        ));
    }

    @PostMapping("/generate-report")
    @Operation(summary = "Generate comprehensive data report")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<ApiResponse<String>> generateDataReport() {
        log.info("Generating comprehensive data report");

        cbcDataService.generateDataReport();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data report generated successfully",
                "Check logs for detailed data report"
        ));
    }

    // ============ HELPER CLASSES ============

    public static class RecordCountResponse {
        private final long totalRecords;
        private final String recordType;
        
        public RecordCountResponse(long totalRecords, String recordType) {
            this.totalRecords = totalRecords;
            this.recordType = recordType;
        }
        
        public long getTotalRecords() { return totalRecords; }
        public String getRecordType() { return recordType; }
    }

    public static class BatchSessionMoveResponse {
        private String batchSessionId;
        private String message;
        
        public String getBatchSessionId() { return batchSessionId; }
        public void setBatchSessionId(String batchSessionId) { this.batchSessionId = batchSessionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}