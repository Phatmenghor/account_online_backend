package com.internal.feature.report_staging.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.response.CbcMainRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.service.CbcDataService;
import com.internal.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/load")
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

    @GetMapping("/load-status")
    public ResponseEntity<ApiResponse<DataLoadStatusDto>> getLoadStatus() {
        log.info("Fetching current load status");

        DataLoadStatusDto status = cbcDataService.getLoadStatus();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Load status retrieved successfully",
                status
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CbcMainRecordResponseDto>>> getAllCbcRecords() {
        log.info("Fetching all CBC records without pagination");

        List<CbcMainRecordResponseDto> records = cbcDataService.getAllCbcRecords();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("CBC records retrieved successfully. Total: %d", records.size()),
                records
        ));
    }

    @PostMapping("/paginated")
    public ResponseEntity<ApiResponse<PaginationResponse<CbcMainRecordResponseDto>>> getCbcRecordsPaginated(
            @Valid @RequestBody CbcFilterRequestDto filterRequest) {
        log.info("Fetching CBC records with pagination - Page: {}, Size: {}",
                filterRequest.getPage(), filterRequest.getSize());

        PaginationResponse<CbcMainRecordResponseDto> paginatedRecords = 
                cbcDataService.getCbcRecordsPaginated(filterRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                String.format("CBC records retrieved successfully. Page %d/%d", 
                        paginatedRecords.getPageNo(), paginatedRecords.getTotalPages()),
                paginatedRecords
        ));
    }

    @PostMapping("/by-id")
    public ResponseEntity<ApiResponse<CbcMainRecordResponseDto>> getCbcRecordById(@RequestBody IdRequest request) {
        log.info("Fetching CBC record by ID: {}", request.getId());

        CbcMainRecordResponseDto record = cbcDataService.getCbcRecordById(request.getId());

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "CBC record retrieved successfully",
                record
        ));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CbcMainRecordResponseDto>> updateCbcRecord(@Valid @RequestBody UpdateRequest request) {
        log.info("Updating CBC record with ID: {}", request.getId());

        CbcMainRecordResponseDto updatedRecord = cbcDataService.updateCbcRecord(request.getId(), request.getUpdateData());

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "CBC record updated successfully",
                updatedRecord
        ));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteCbcRecord(@RequestBody IdRequest request) {
        log.info("Deleting CBC record with ID: {}", request.getId());

        cbcDataService.deleteCbcRecord(request.getId());

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "CBC record deleted successfully",
                "Record deleted"
        ));
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAllCbcRecords() {
        log.info("Deleting all CBC records");

        cbcDataService.deleteAllCbcRecords();

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "All CBC records deleted successfully",
                "All records deleted"
        ));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<RecordCountResponse>> getTotalRecordsCount() {
        log.info("Fetching total records count");

        long count = cbcDataService.getTotalRecordsCount();

        RecordCountResponse countResponse = new RecordCountResponse(count);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Total records count retrieved successfully",
                countResponse
        ));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<CbcMainRecordResponseDto>>> searchCbcRecords(
            @Valid @RequestBody CbcFilterRequestDto searchRequest) {
        log.info("Searching CBC records with filters");

        PaginationResponse<CbcMainRecordResponseDto> searchResults = 
                cbcDataService.getCbcRecordsPaginated(searchRequest);

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Search completed successfully",
                searchResults
        ));
    }

    // Helper DTOs
    public static class IdRequest {
        private Long id;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class UpdateRequest {
        private Long id;
        private CbcUpdateRequestDto updateData;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public CbcUpdateRequestDto getUpdateData() { return updateData; }
        public void setUpdateData(CbcUpdateRequestDto updateData) { this.updateData = updateData; }
    }

    public static class RecordCountResponse {
        private final long totalRecords;
        
        public RecordCountResponse(long totalRecords) {
            this.totalRecords = totalRecords;
        }
        
        public long getTotalRecords() { return totalRecords; }
    }
}