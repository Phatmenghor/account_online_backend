package com.internal.feature.report_staging.service;

import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.MoveToFinalRequestDto;
import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.update.CbcBulkUpdateRequestDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.utils.pagination.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface CbcFinalService {

    // ============ BATCH OPERATIONS ============
    String moveStagingToFinal(MoveToFinalRequestDto request);
    List<BatchSessionResponseDto> getAllBatchSessions();
    List<BatchSessionResponseDto> getBatchSessionsWithFilter(CbcFilterRequestDto filterRequest);

    // ============ RECORD RETRIEVAL OPERATIONS ============
    PaginationResponse<CbcRecordResponseDto> getFinalRecordsPaginated(CbcFilterRequestDto filterRequest);
    List<CbcRecordResponseDto> getAllFinalRecords(CbcFilterRequestDto filterRequest);
    CbcRecordResponseDto getFinalRecordById(UUID id);
    List<CbcRecordResponseDto> getFinalRecordsByIds(List<UUID> ids);
    long getFinalRecordsCount();
    long getActiveFinalRecordsCount();

    // ============ RECORD UPDATE OPERATIONS ============
    CbcRecordResponseDto updateFinalRecord(UUID id, CbcUpdateRequestDto updateRequest);
    List<CbcRecordResponseDto> updateMultipleFinalRecords(CbcBulkUpdateRequestDto bulkUpdateRequest);

    // ============ ARCHIVE OPERATIONS ============
    void archiveRecords(List<UUID> ids);
    void softDeleteRecords(List<UUID> ids);
    void restoreRecords(List<UUID> ids);

    // ============ SEARCH OPERATIONS ============
    List<CbcRecordResponseDto> searchByAccountNumber(String accountNumber);
    List<CbcRecordResponseDto> searchByCreditorId(String creditorId);
    List<CbcRecordResponseDto> searchByBatchSession(String batchSessionId);

    // ============ STATISTICS OPERATIONS ============
    long getRecordCountByBatchSession(String batchSessionId);
    long getUpdatedRecordsCount();
}