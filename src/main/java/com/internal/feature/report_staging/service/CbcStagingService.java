
package com.internal.feature.report_staging.service;

import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcBulkUpdateRequestDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.utils.pagination.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface CbcStagingService {

    // ============ DATA LOADING OPERATIONS ============
    DataLoadStatusDto loadCbcData(CbcDataRequestDto request);
    DataLoadStatusDto getLoadStatus();
    void clearStagingDataSync();

    // ============ RECORD RETRIEVAL OPERATIONS ============
    PaginationResponse<CbcRecordResponseDto> getStagingRecordsPaginated(CbcFilterRequestDto filterRequest);
    List<CbcRecordResponseDto> getAllStagingRecords(CbcFilterRequestDto filterRequest);
    CbcRecordResponseDto getStagingRecordById(UUID id);
    List<CbcRecordResponseDto> getStagingRecordsByIds(List<UUID> ids);
    long getStagingRecordsCount();
    long getUpdatedRecordsCount();

    // ============ RECORD UPDATE OPERATIONS ============
    CbcRecordResponseDto updateStagingRecord(UUID id, CbcUpdateRequestDto updateRequest);
    List<CbcRecordResponseDto> updateMultipleStagingRecords(CbcBulkUpdateRequestDto bulkUpdateRequest);
    
    // ============ VALIDATION OPERATIONS ============
    CbcRecordResponseDto validateStagingRecord(UUID id);
    List<CbcRecordResponseDto> validateMultipleStagingRecords(List<UUID> ids);
    long getValidationSummary(String status);

    // ============ SEARCH OPERATIONS ============
    List<CbcRecordResponseDto> searchByAccountNumber(String accountNumber);
    List<CbcRecordResponseDto> searchByCreditorId(String creditorId);
    List<CbcRecordResponseDto> searchByValidationStatus(String status);
}