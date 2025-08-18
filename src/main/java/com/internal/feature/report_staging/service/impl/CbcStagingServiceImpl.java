
package com.internal.feature.report_staging.service.impl;

import com.internal.exceptions.error.BadRequestException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcBulkUpdateRequestDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.mapper.CbcMapper;
import com.internal.feature.report_staging.mapper.CbcResultSetMapper;
import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import com.internal.feature.report_staging.repository.CbcStagingRepository;
import com.internal.feature.report_staging.service.CbcStagingService;
import com.internal.feature.report_staging.specification.CbcStagingRecordSpecification;
import com.internal.utils.SecurityUtils;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbcStagingServiceImpl implements CbcStagingService {

    private final CbcStagingRepository stagingRepository;
    private final CbcMapper cbcMapper;
    private final CbcResultSetMapper resultSetMapper;
    private final SecurityUtils securityUtils;

    @Value("${spring.datasource.secondary.url}")
    private String dbUrl;

    @Value("${spring.datasource.secondary.username}")
    private String dbUser;

    @Value("${spring.datasource.secondary.password}")
    private String dbPassword;

    @Value("${spring.datasource.secondary.driver-class-name}")
    private String dbDriver;

    @Value("${app.data-load.batch-size:1000}")
    private int batchSize;

    // Load status tracking
    private volatile DataLoadStatusDto currentLoadStatus = new DataLoadStatusDto(
            "IDLE", "No data loading operation in progress", 0L, null, null, null);

    // ============ DATA LOADING OPERATIONS ============

    @Override
    public DataLoadStatusDto loadCbcData(CbcDataRequestDto request) {
        log.info("Starting CBC data loading for date range: {} to {}", 
                request.getStartDate(), request.getEndDate());

        // Check if already loading
        if ("LOADING".equals(currentLoadStatus.getStatus())) {
            log.warn("Data loading already in progress");
            return currentLoadStatus;
        }

        // Start async loading
        loadDataAsync(request);
        
        return currentLoadStatus;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> loadDataAsync(CbcDataRequestDto request) {
        String currentUser = getCurrentUsername();
        LocalDateTime startTime = LocalDateTime.now();
        
        updateLoadStatus("LOADING", "Data loading in progress", 0L, null, startTime, null);

        try {
            // Clear existing staging data
            log.info("Clearing existing staging data");
            stagingRepository.deleteAllStaging();

            // Load data from SQL Server with date parameters
            List<CbcStagingRecordEntity> stagingRecords = loadDataFromSqlServer(
                    request.getStartDate().toString(), 
                    request.getEndDate().toString(), 
                    currentUser);
            
            // Save in batches
            saveInBatches(stagingRecords);
            
            LocalDateTime endTime = LocalDateTime.now();
            updateLoadStatus("SUCCESS", "Data loaded successfully", 
                    (long) stagingRecords.size(), endTime, startTime, endTime);
            
            log.info("CBC data loading completed successfully. Loaded {} records", stagingRecords.size());
            
        } catch (Exception e) {
            log.error("Error during CBC data loading: {}", e.getMessage(), e);
            LocalDateTime endTime = LocalDateTime.now();
            updateLoadStatus("FAILED", "Data loading failed: " + e.getMessage(), 
                    0L, endTime, startTime, endTime);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public DataLoadStatusDto getLoadStatus() {
        return currentLoadStatus;
    }

    @Override
    @Transactional
    public void clearStagingDataSync() {
        log.info("Clearing all staging data synchronously");
        stagingRepository.deleteAllStaging();
        log.info("Staging data cleared successfully");
    }

    // ============ RECORD RETRIEVAL OPERATIONS ============

    @Override
    public PaginationResponse<CbcRecordResponseDto> getStagingRecordsPaginated(CbcFilterRequestDto filterRequest) {
        log.info("Fetching staging records with pagination. Page: {}, Size: {}", 
                filterRequest.getPage(), filterRequest.getSize());

        Specification<CbcStagingRecordEntity> spec = CbcStagingRecordSpecification.withFilters(
                filterRequest.getSearch()
        );

        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                          filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage() - 1, filterRequest.getSize(), sort);

        Page<CbcStagingRecordEntity> page = stagingRepository.findAll(spec, pageable);

        List<CbcRecordResponseDto> content = page.getContent().stream()
                .map(cbcMapper::stagingToResponseDto)
                .collect(Collectors.toList());

        return createPaginationResponse(content, page, filterRequest);
    }

    @Override
    public List<CbcRecordResponseDto> getAllStagingRecords(CbcFilterRequestDto filterRequest) {
        log.info("Fetching all staging records without pagination");

        Specification<CbcStagingRecordEntity> spec = CbcStagingRecordSpecification.withFilters(
                filterRequest.getSearch()
        );

        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                          filterRequest.getSortBy());

        List<CbcStagingRecordEntity> records = stagingRepository.findAll(spec, sort);
        
        return records.stream()
                .map(cbcMapper::stagingToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CbcRecordResponseDto getStagingRecordById(UUID id) {
        log.info("Fetching staging record by ID: {}", id);
        CbcStagingRecordEntity entity = stagingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staging record not found with ID: " + id));
        return cbcMapper.stagingToResponseDto(entity);
    }

    @Override
    public List<CbcRecordResponseDto> getStagingRecordsByIds(List<UUID> ids) {
        log.info("Fetching staging records by IDs: {}", ids);
        List<CbcStagingRecordEntity> entities = stagingRepository.findByIds(ids);
        return cbcMapper.stagingListToResponseDtoList(entities);
    }

    @Override
    public long getStagingRecordsCount() {
        return stagingRepository.countAllStaging();
    }

    @Override
    public long getUpdatedRecordsCount() {
        return stagingRepository.countUpdatedRecords();
    }

    // ============ RECORD UPDATE OPERATIONS ============

    @Override
    @Transactional
    public CbcRecordResponseDto updateStagingRecord(UUID id, CbcUpdateRequestDto updateRequest) {
        log.info("Updating staging record with ID: {}", id);
        
        String currentUser = getCurrentUsername();
        
        CbcStagingRecordEntity entity = stagingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staging record not found with ID: " + id));

        // Update using mapper
        cbcMapper.updateStagingFromDto(updateRequest, entity);
        entity.setUpdatedBy(currentUser);

        CbcStagingRecordEntity savedEntity = stagingRepository.save(entity);
        log.info("Staging record updated successfully with ID: {}", id);
        
        return cbcMapper.stagingToResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public List<CbcRecordResponseDto> updateMultipleStagingRecords(CbcBulkUpdateRequestDto bulkUpdateRequest) {
        log.info("Processing bulk update for {} staging records", bulkUpdateRequest.getUpdates().size());
        
        String currentUser = getCurrentUsername();
        List<CbcRecordResponseDto> updatedRecords = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (CbcUpdateRequestDto updateRequest : bulkUpdateRequest.getUpdates()) {
            try {
                List<UUID> targetIds = updateRequest.getIds() != null && !updateRequest.getIds().isEmpty() 
                    ? updateRequest.getIds() 
                    : List.of(updateRequest.getId());

                for (UUID id : targetIds) {
                    if (id != null) {
                        CbcRecordResponseDto updated = updateStagingRecord(id, updateRequest);
                        updatedRecords.add(updated);
                    }
                }
            } catch (Exception e) {
                String error = String.format("Failed to update staging record %s: %s", 
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

        log.info("Bulk update completed. Updated {} staging records with {} errors", 
                updatedRecords.size(), errors.size());
        
        return updatedRecords;
    }

    // ============ VALIDATION OPERATIONS ============

    @Override
    @Transactional
    public CbcRecordResponseDto validateStagingRecord(UUID id) {
        log.info("Validating staging record with ID: {}", id);
        
        CbcStagingRecordEntity entity = stagingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staging record not found with ID: " + id));

        // Simple validation logic
        StringBuilder errors = new StringBuilder();
        String status = "VALIDATED";

        if (entity.getAccountNumber() == null || entity.getAccountNumber().trim().isEmpty()) {
            errors.append("Account number is required; ");
            status = "INVALID";
        }

        if (entity.getCreditorId() == null || entity.getCreditorId().trim().isEmpty()) {
            errors.append("Creditor ID is required; ");
            status = "INVALID";
        }

        // Add more validation rules as needed
        if (entity.getFirstNameEnglish() == null || entity.getFirstNameEnglish().trim().isEmpty()) {
            if (entity.getUnformattedNameEnglish() == null || entity.getUnformattedNameEnglish().trim().isEmpty()) {
                errors.append("Either first name or unformatted name in English is required; ");
                status = "INVALID";
            }
        }

        entity.setValidationStatus(status);
        entity.setValidationErrors(errors.length() > 0 ? errors.toString() : null);
        
        CbcStagingRecordEntity savedEntity = stagingRepository.save(entity);
        return cbcMapper.stagingToResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public List<CbcRecordResponseDto> validateMultipleStagingRecords(List<UUID> ids) {
        log.info("Validating {} staging records", ids.size());
        
        return ids.stream()
                .map(this::validateStagingRecord)
                .collect(Collectors.toList());
    }

    @Override
    public long getValidationSummary(String status) {
        return stagingRepository.countByValidationStatus(status);
    }

    // ============ SEARCH OPERATIONS ============

    @Override
    public List<CbcRecordResponseDto> searchByAccountNumber(String accountNumber) {
        log.info("Searching staging records by account number: {}", accountNumber);
        List<CbcStagingRecordEntity> entities = stagingRepository.findByAccountNumber(accountNumber);
        return cbcMapper.stagingListToResponseDtoList(entities);
    }

    @Override
    public List<CbcRecordResponseDto> searchByCreditorId(String creditorId) {
        log.info("Searching staging records by creditor ID: {}", creditorId);
        List<CbcStagingRecordEntity> entities = stagingRepository.findByCreditorId(creditorId);
        return cbcMapper.stagingListToResponseDtoList(entities);
    }

    @Override
    public List<CbcRecordResponseDto> searchByValidationStatus(String status) {
        log.info("Searching staging records by validation status: {}", status);
        List<CbcStagingRecordEntity> entities = stagingRepository.findByValidationStatus(status);
        return cbcMapper.stagingListToResponseDtoList(entities);
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

    private void updateLoadStatus(String status, String message, Long totalRecords, 
                                LocalDateTime lastLoadTime, LocalDateTime startTime, LocalDateTime endTime) {
        this.currentLoadStatus = new DataLoadStatusDto(status, message, totalRecords, 
                lastLoadTime, startTime, endTime);
    }

    private String buildCbcDataQuery() {
        return """
            SELECT * FROM dbo.CBC_EXPORT_VIEW 
            WHERE CAST(AsofDate AS DATE) BETWEEN ? AND ?
            ORDER BY AsofDate DESC, [Account Number]
            """;
    }

    private List<CbcStagingRecordEntity> loadDataFromSqlServer(String startDate, String endDate, String currentUser) 
            throws SQLException, ClassNotFoundException {
        
        log.info("Loading data from SQL Server for date range: {} to {}", startDate, endDate);
        List<CbcStagingRecordEntity> records = new ArrayList<>();
        
        Class.forName(dbDriver);
        
        String query = buildCbcDataQuery();
        
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(query)) {
             
            // Set date parameters
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            log.info("Executing query with parameters: startDate={}, endDate={}", startDate, endDate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CbcStagingRecordEntity entity = resultSetMapper.mapResultSetToStagingEntity(rs, currentUser);
                    records.add(entity);
                    
                    if (records.size() % 1000 == 0) {
                        log.info("Loaded {} records so far", records.size());
                    }
                }
            }
        }
        
        log.info("Loaded {} records from SQL Server", records.size());
        return records;
    }

    @Transactional
    public void saveInBatches(List<CbcStagingRecordEntity> records) {
        for (int i = 0; i < records.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, records.size());
            List<CbcStagingRecordEntity> batch = records.subList(i, endIndex);
            
            try {
                stagingRepository.saveAll(batch);
                log.info("Saved batch {}/{} - {} records", 
                        (i / batchSize) + 1, 
                        (records.size() + batchSize - 1) / batchSize, 
                        batch.size());
            } catch (Exception e) {
                log.error("Error saving batch starting at index {}: {}", i, e.getMessage());
                throw new RuntimeException("Failed to save staging records batch", e);
            }
        }
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
}