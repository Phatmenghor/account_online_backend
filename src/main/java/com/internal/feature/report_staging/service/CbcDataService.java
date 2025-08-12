package com.internal.feature.report_staging.service;

import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.response.CbcMainRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.utils.pagination.PaginationResponse;

import java.util.List;

public interface CbcDataService {

    /**
     * Load data from SQL Server stored procedure and store in PostgreSQL
     * @param request Date range request
     * @return Status of data loading operation
     */
    DataLoadStatusDto loadCbcData(CbcDataRequestDto request);

    /**
     * Get current status of data loading operation
     * @return Current load status
     */
    DataLoadStatusDto getLoadStatus();

    /**
     * Get all CBC records without pagination
     * @return List of all CBC records
     */
    List<CbcMainRecordResponseDto> getAllCbcRecords();

    /**
     * Get CBC records with pagination and filtering
     * @param filterRequest Filter and pagination parameters
     * @return Paginated CBC records
     */
    PaginationResponse<CbcMainRecordResponseDto> getCbcRecordsPaginated(CbcFilterRequestDto filterRequest);

    /**
     * Get CBC record by ID
     * @param id Record ID
     * @return CBC record details
     */
    CbcMainRecordResponseDto getCbcRecordById(Long id);

    /**
     * Update CBC record
     * @param id Record ID
     * @param updateRequest Update data
     * @return Updated CBC record
     */
    CbcMainRecordResponseDto updateCbcRecord(Long id, CbcUpdateRequestDto updateRequest);

    /**
     * Delete CBC record by ID
     * @param id Record ID
     */
    void deleteCbcRecord(Long id);

    /**
     * Delete all CBC records
     */
    void deleteAllCbcRecords();

    /**
     * Get total count of records
     * @return Total number of records
     */
    long getTotalRecordsCount();
}