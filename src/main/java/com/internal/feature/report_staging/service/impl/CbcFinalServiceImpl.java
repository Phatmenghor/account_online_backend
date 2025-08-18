package com.internal.feature.report_staging.service.impl;

import com.internal.exceptions.error.BadRequestException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.MoveToFinalRequestDto;
import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.update.CbcBulkUpdateRequestDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.mapper.BatchSessionMapper;
import com.internal.feature.report_staging.mapper.CbcMapper;
import com.internal.feature.report_staging.models.CbcFinalRecordEntity;
import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import com.internal.feature.report_staging.repository.CbcFinalRepository;
import com.internal.feature.report_staging.repository.CbcStagingRepository;
import com.internal.feature.report_staging.service.CbcFinalService;
import com.internal.feature.report_staging.specification.CbcFinalRecordSpecification;
import com.internal.utils.SecurityUtils;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbcFinalServiceImpl implements CbcFinalService {

    private final CbcFinalRepository finalRepository;
    private final CbcStagingRepository stagingRepository;
    private final CbcMapper cbcMapper;
    private final BatchSessionMapper batchSessionMapper;
    private final SecurityUtils securityUtils;

    @Value("${app.data-load.batch-size:1000}")
    private int batchSize;

    // ============ BATCH OPERATIONS ============

    @Override
    @Transactional
    public String moveStagingToFinal(MoveToFinalRequestDto request) {
        log.info("Moving all staging records to final table");
        
        List<CbcStagingRecordEntity> stagingRecords = stagingRepository.findAll();
        
        if (stagingRecords.isEmpty()) {
            throw new BadRequestException("No staging records found to move to final");
        }

        String batchSessionId = generateBatchSessionId();
        String currentUser = getCurrentUsername();
        LocalDate batchDate = LocalDate.now();
        LocalDateTime processedTime = LocalDateTime.now();

        List<CbcFinalRecordEntity> finalRecords = stagingRecords.stream()
                .map(staging -> convertStagingToFinal(staging, batchSessionId, batchDate, processedTime, currentUser))
                .collect(Collectors.toList());

        // Save to final table in batches
        saveInBatches(finalRecords);
        
        // Clear staging table
        stagingRepository.deleteAllStaging();
        
        log.info("Successfully moved {} records to final table with batch session: {}", 
                finalRecords.size(), batchSessionId);
        
        return batchSessionId;
    }

    @Override
    public List<BatchSessionResponseDto> getAllBatchSessions() {
        log.info("Fetching all batch sessions");
        
        List<Object[]> results = finalRepository.findAllBatchSessionsSummary();
        
        return results.stream()
                .map(batchSessionMapper::mapToBatchSessionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BatchSessionResponseDto> getBatchSessionsWithFilter(CbcFilterRequestDto filterRequest) {
        log.info("Fetching batch sessions with filters");
        
        List<Object[]> results;
        
        if (hasDateFilters(filterRequest)) {
            results = finalRepository.findBatchSessionsSummaryByDateRange(
                    filterRequest.getStartDate(), filterRequest.getEndDate());
        } else if (hasSearchFilter(filterRequest)) {
            results = finalRepository.findBatchSessionsSummaryBySearch(filterRequest.getSearch());
        } else {
            results = finalRepository.findAllBatchSessionsSummary();
        }
        
        return results.stream()
                .map(batchSessionMapper::mapToBatchSessionDto)
                .collect(Collectors.toList());
    }

    // ============ RECORD RETRIEVAL OPERATIONS ============

    @Override
    public PaginationResponse<CbcRecordResponseDto> getFinalRecordsPaginated(CbcFilterRequestDto filterRequest) {
        log.info("Fetching final records with pagination. Page: {}, Size: {}", 
                filterRequest.getPage(), filterRequest.getSize());

        Specification<CbcFinalRecordEntity> spec = CbcFinalRecordSpecification.withFilters(
                filterRequest.getStartDate(),
                filterRequest.getEndDate(),
                filterRequest.getBatchSessionId(),
                filterRequest.getSearch()
        );

        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                          filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage() - 1, filterRequest.getSize(), sort);

        Page<CbcFinalRecordEntity> page = finalRepository.findAll(spec, pageable);

        List<CbcRecordResponseDto> content = page.getContent().stream()
                .map(cbcMapper::finalToResponseDto)
                .collect(Collectors.toList());

        return createPaginationResponse(content, page, filterRequest);
    }

    @Override
    public List<CbcRecordResponseDto> getAllFinalRecords(CbcFilterRequestDto filterRequest) {
        log.info("Fetching all final records without pagination");

        Specification<CbcFinalRecordEntity> spec = CbcFinalRecordSpecification.withFilters(
                filterRequest.getStartDate(),
                filterRequest.getEndDate(),
                filterRequest.getBatchSessionId(),
                filterRequest.getSearch()
        );

        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                          filterRequest.getSortBy());

        List<CbcFinalRecordEntity> records = finalRepository.findAll(spec, sort);
        
        return records.stream()
                .map(cbcMapper::finalToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CbcRecordResponseDto getFinalRecordById(UUID id) {
        log.info("Fetching final record by ID: {}", id);
        CbcFinalRecordEntity entity = finalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Final record not found with ID: " + id));
        return cbcMapper.finalToResponseDto(entity);
    }

    @Override
    public List<CbcRecordResponseDto> getFinalRecordsByIds(List<UUID> ids) {
        log.info("Fetching final records by IDs: {}", ids);
        List<CbcFinalRecordEntity> entities = finalRepository.findByIds(ids);
        return cbcMapper.finalListToResponseDtoList(entities);
    }

    @Override
    public long getFinalRecordsCount() {
        return finalRepository.countAllFinal();
    }

    @Override
    public long getActiveFinalRecordsCount() {
        return finalRepository.countActiveFinal();
    }

    // ============ RECORD UPDATE OPERATIONS ============

    @Override
    @Transactional
    public CbcRecordResponseDto updateFinalRecord(UUID id, CbcUpdateRequestDto updateRequest) {
        log.info("Updating final record with ID: {}", id);
        
        String currentUser = getCurrentUsername();
        
        CbcFinalRecordEntity entity = finalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Final record not found with ID: " + id));

        // Update using mapper
        cbcMapper.updateFinalFromDto(updateRequest, entity);
        entity.setUpdatedBy(currentUser);

        CbcFinalRecordEntity savedEntity = finalRepository.save(entity);
        log.info("Final record updated successfully with ID: {}", id);
        
        return cbcMapper.finalToResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public List<CbcRecordResponseDto> updateMultipleFinalRecords(CbcBulkUpdateRequestDto bulkUpdateRequest) {
        log.info("Processing bulk update for {} final records", bulkUpdateRequest.getUpdates().size());
        
        String currentUser = getCurrentUsername();
        List<CbcRecordResponseDto> updatedRecords = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (CbcUpdateRequestDto updateRequest : bulkUpdateRequest.getUpdates()) {
            try {
                List<UUID> targetIds = updateRequest.getIds() != null && !updateRequest.getIds().isEmpty()
                        ? updateRequest.getIds()
                        : Collections.singletonList(updateRequest.getId());

                for (UUID id : targetIds) {
                    if (id != null) {
                        CbcRecordResponseDto updated = updateFinalRecord(id, updateRequest);
                        updatedRecords.add(updated);
                    }
                }
            } catch (Exception e) {
                String error = String.format("Failed to update final record %s: %s", 
                    updateRequest.getId(), e.getMessage());
                errors.add(error);
                log.warn(error);
                
                if (!bulkUpdateRequest.isContinueOnError()) {
                    throw new BadRequestException("Bulk update failed: " + error);
                }
            }
        }

        if (!errors.isEmpty()) {
            log.warn("Bulk update completed with {} errors: {}", errors.size(), errors);
        }

        log.info("Bulk update completed. Updated {} final records with {} errors", 
                updatedRecords.size(), errors.size());
        
        return updatedRecords;
    }

    // ============ ARCHIVE OPERATIONS ============

    @Override
    @Transactional
    public void archiveRecords(List<UUID> ids) {
        log.info("Archiving {} final records", ids.size());
        String currentUser = getCurrentUsername();
        finalRepository.archiveRecords(ids, currentUser);
        log.info("Successfully archived {} records", ids.size());
    }

    @Override
    @Transactional
    public void softDeleteRecords(List<UUID> ids) {
        log.info("Soft deleting {} final records", ids.size());
        String currentUser = getCurrentUsername();
        finalRepository.softDeleteRecords(ids, currentUser);
        log.info("Successfully soft deleted {} records", ids.size());
    }

    @Override
    @Transactional
    public void restoreRecords(List<UUID> ids) {
        log.info("Restoring {} final records", ids.size());
        String currentUser = getCurrentUsername();
        
        List<CbcFinalRecordEntity> entities = finalRepository.findAllById(ids);
        entities.forEach(entity -> {
            entity.setArchiveStatus("ACTIVE");
            entity.setUpdatedBy(currentUser);
        });
        
        finalRepository.saveAll(entities);
        log.info("Successfully restored {} records", ids.size());
    }

    // ============ SEARCH OPERATIONS ============

    @Override
    public List<CbcRecordResponseDto> searchByAccountNumber(String accountNumber) {
        log.info("Searching final records by account number: {}", accountNumber);
        List<CbcFinalRecordEntity> entities = finalRepository.findByAccountNumber(accountNumber);
        return cbcMapper.finalListToResponseDtoList(entities);
    }

    @Override
    public List<CbcRecordResponseDto> searchByCreditorId(String creditorId) {
        log.info("Searching final records by creditor ID: {}", creditorId);
        List<CbcFinalRecordEntity> entities = finalRepository.findByCreditorId(creditorId);
        return cbcMapper.finalListToResponseDtoList(entities);
    }

    @Override
    public List<CbcRecordResponseDto> searchByBatchSession(String batchSessionId) {
        log.info("Searching final records by batch session: {}", batchSessionId);
        List<CbcFinalRecordEntity> entities = finalRepository.findByBatchSessionId(batchSessionId);
        return cbcMapper.finalListToResponseDtoList(entities);
    }

    // ============ STATISTICS OPERATIONS ============

    @Override
    public long getRecordCountByBatchSession(String batchSessionId) {
        return finalRepository.countByBatchSession(batchSessionId);
    }

    @Override
    public long getUpdatedRecordsCount() {
        return finalRepository.countUpdatedRecords();
    }

    // ============ PRIVATE HELPER METHODS ============

    private String getCurrentUsername() {
        try {
            return securityUtils.getCurrentUser().getUsername();
        } catch (Exception e) {
            log.warn("Could not get current user, using system user: {}", e.getMessage());
            return "SYSTEM";
        }
    }

    private String generateBatchSessionId() {
        return "BATCH_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private boolean hasDateFilters(CbcFilterRequestDto filterRequest) {
        return filterRequest.getStartDate() != null && filterRequest.getEndDate() != null;
    }

    private boolean hasSearchFilter(CbcFilterRequestDto filterRequest) {
        return filterRequest.getSearch() != null && !filterRequest.getSearch().trim().isEmpty();
    }

    private PaginationResponse<CbcRecordResponseDto> createPaginationResponse(
            List<CbcRecordResponseDto> content, 
            Page<?> page, 
            CbcFilterRequestDto filterRequest) {
        
        PaginationResponse<CbcRecordResponseDto> response = new PaginationResponse<>();
        response.setContent(content);
        response.setPageNo(filterRequest.getPage());
        response.setPageSize(filterRequest.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setTotalCount((int) page.getTotalElements());
        return response;
    }

    private CbcFinalRecordEntity convertStagingToFinal(CbcStagingRecordEntity staging, 
                                                       String batchSessionId, 
                                                       LocalDate batchDate, 
                                                       LocalDateTime processedTime, 
                                                       String currentUser) {
        CbcFinalRecordEntity finalRecord = new CbcFinalRecordEntity();
        
        // Copy all fields from staging to final using BeanUtils
        BeanUtils.copyProperties(staging, finalRecord, "id", "createdAt", "updatedAt");
        
        // Set batch session info
        finalRecord.setBatchSessionId(batchSessionId);
        finalRecord.setBatchSessionDate(batchDate);
        finalRecord.setProcessedDate(processedTime);
        finalRecord.setOriginalLoadDate(staging.getCreatedAt() != null ? 
                staging.getCreatedAt().toLocalDate() : LocalDate.now());
        finalRecord.setWasUpdated(staging.getIsUpdated());
        finalRecord.setUpdateCount(0);
        finalRecord.setArchiveStatus("ACTIVE");
        
        finalRecord.setCreatedBy(currentUser);
        finalRecord.setUpdatedBy(currentUser);
        
        return finalRecord;
    }

    @Transactional
    public void saveInBatches(List<CbcFinalRecordEntity> finalRecords) {
        for (int i = 0; i < finalRecords.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, finalRecords.size());
            List<CbcFinalRecordEntity> batch = finalRecords.subList(i, endIndex);
            
            try {
                finalRepository.saveAll(batch);
                log.info("Saved batch {}/{} - {} records", 
                        (i / batchSize) + 1, 
                        (finalRecords.size() + batchSize - 1) / batchSize, 
                        batch.size());
            } catch (Exception e) {
                log.error("Error saving batch starting at index {}: {}", i, e.getMessage());
                throw new RuntimeException("Failed to save final records batch", e);
            }
        }
    }
}