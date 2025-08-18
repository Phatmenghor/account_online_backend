package com.internal.feature.report_staging.service;

import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.utils.pagination.PaginationResponse;

import java.util.List;

public interface CbcDataService {

    // ============ STAGING OPERATIONS ============
    DataLoadStatusDto loadCbcData(CbcDataRequestDto request);
    DataLoadStatusDto getLoadStatus();
    PaginationResponse<CbcRecordResponseDto> getStagingRecordsPaginated(CbcFilterRequestDto filterRequest);
    CbcRecordResponseDto getStagingRecordById(Long id);
    CbcRecordResponseDto updateStagingRecord(Long id, CbcUpdateRequestDto updateRequest);
    long getStagingRecordsCount();
    void clearStagingDataSync();

    // ============ FINAL/HISTORY OPERATIONS ============
    String moveStagingToFinal();
    PaginationResponse<CbcRecordResponseDto> getFinalRecordsPaginated(CbcFilterRequestDto filterRequest);
    List<BatchSessionResponseDto> getAllBatchSessions();
    List<BatchSessionResponseDto> getBatchSessionsWithFilter(CbcFilterRequestDto filterRequest);
    long getFinalRecordsCount();
}
