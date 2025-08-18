package com.internal.feature.report_staging.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.MoveToFinalRequestDto;
import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.service.CbcDataService;
import com.internal.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cbc-data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CBC Data Management")
public class CbcDataController {

    private final CbcDataService cbcDataService;

    // ============ STAGING OPERATIONS ============

    @PostMapping("/staging/load")
    @Operation(summary = "Load CBC data from SQL Server to staging table")
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

    @GetMapping("/staging/records/{id}")
    @Operation(summary = "Get single staging record by ID")
    public ResponseEntity<ApiResponse<CbcRecordResponseDto>> getStagingRecordById(@PathVariable Long id) {
        log.info("Fetching staging record by ID: {}", id);

        CbcRecordResponseDto record = cbcDataService.getStagingRecordById(id);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Staging record retrieved successfully",
                record
        ));
    }

    @PutMapping("/staging/records/{id}")
    @Operation(summary = "Update staging record")
    public ResponseEntity<ApiResponse<CbcRecordResponseDto>> updateStagingRecord(
            @PathVariable Long id, 
            @Valid @RequestBody CbcUpdateRequestDto updateRequest) {
        log.info("Updating staging record with ID: {}", id);

        CbcRecordResponseDto updatedRecord = cbcDataService.updateStagingRecord(id, updateRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Staging record updated successfully",
                updatedRecord
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

    @DeleteMapping("/staging/clear")
    @Operation(summary = "Clear all staging data")
    public ResponseEntity<ApiResponse<String>> clearStagingData() {
        log.info("Clearing all staging data");

        cbcDataService.clearStagingDataSync();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Staging data cleared successfully",
                "All staging records deleted"
        ));
    }

    // ============ FINAL/HISTORY OPERATIONS ============

    @PostMapping("/staging/move-to-final")
    @Operation(summary = "Move all staging records to final table and create batch session")
    public ResponseEntity<ApiResponse<BatchSessionMoveResponse>> moveStagingToFinal(
            @RequestBody(required = false) MoveToFinalRequestDto request) {
        log.info("Moving staging records to final table");

        String batchSessionId = cbcDataService.moveStagingToFinal();
        
        BatchSessionMoveResponse response = new BatchSessionMoveResponse();
        response.setBatchSessionId(batchSessionId);
        response.setMessage("Records moved to final table successfully");

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Records moved to final table successfully with batch session: " + batchSessionId,
                response
        ));
    }

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