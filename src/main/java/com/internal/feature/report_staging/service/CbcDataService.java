package com.internal.feature.report_staging.service;

import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.request.MoveToFinalRequestDto;
import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcBulkUpdateRequestDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.utils.pagination.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface CbcDataService {

    // ============ STAGING OPERATIONS (Delegated) ============
    DataLoadStatusDto loadCbcData(CbcDataRequestDto request);
    DataLoadStatusDto getLoadStatus();
    PaginationResponse<CbcRecordResponseDto> getStagingRecordsPaginated(CbcFilterRequestDto filterRequest);
    List<CbcRecordResponseDto> getAllStagingRecords(CbcFilterRequestDto filterRequest);
    CbcRecordResponseDto getStagingRecordById(UUID id);
    CbcRecordResponseDto updateStagingRecord(UUID id, CbcUpdateRequestDto updateRequest);
    List<CbcRecordResponseDto> updateMultipleStagingRecords(CbcBulkUpdateRequestDto bulkUpdateRequest);
    long getStagingRecordsCount();
    void clearStagingDataSync();

    // ============ FINAL OPERATIONS (Delegated) ============
    String moveStagingToFinal(MoveToFinalRequestDto request);
    PaginationResponse<CbcRecordResponseDto> getFinalRecordsPaginated(CbcFilterRequestDto filterRequest);
    List<CbcRecordResponseDto> getAllFinalRecords(CbcFilterRequestDto filterRequest);
    List<BatchSessionResponseDto> getAllBatchSessions();
    List<BatchSessionResponseDto> getBatchSessionsWithFilter(CbcFilterRequestDto filterRequest);
    CbcRecordResponseDto getFinalRecordById(UUID id);
    CbcRecordResponseDto updateFinalRecord(UUID id, CbcUpdateRequestDto updateRequest);
    List<CbcRecordResponseDto> updateMultipleFinalRecords(CbcBulkUpdateRequestDto bulkUpdateRequest);
    long getFinalRecordsCount();

    // ============ CROSS-CUTTING OPERATIONS ============
    void validateDataIntegrity();
    void generateDataReport();
}