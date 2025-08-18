package com.internal.feature.report_staging.service.impl;

import com.internal.exceptions.error.BadRequestException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.auth.models.UserEntity;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbcStagingServiceImpl implements CbcStagingService {

    private final CbcStagingRepository stagingRepository;
    private final CbcMapper cbcMapper;
    private final CbcResultSetMapper resultSetMapper;
    private final SecurityUtils securityUtils;

    // SQL Server Configuration
    @Value("${spring.datasource.secondary.url}")
    private String dbUrl;

    @Value("${spring.datasource.secondary.username}")
    private String dbUser;

    @Value("${spring.datasource.secondary.password}")
    private String dbPassword;

    @Value("${spring.datasource.secondary.driver-class-name}")
    private String dbDriver;

    // 4-Hour Operation Configuration
    @Value("${app.data-load.batch-size:1000}")
    private int batchSize;

    @Value("${app.data-load.timeout-hours:5}")
    private int timeoutHours;

    @Value("${app.data-load.connection-timeout-minutes:5}")
    private int connectionTimeoutMinutes;

    @Value("${app.data-load.query-timeout-hours:0}")
    private int queryTimeoutHours; // 0 = unlimited

    @Value("${app.data-load.progress-log-interval-minutes:10}")
    private int progressLogIntervalMinutes;

    @Value("${app.data-load.heartbeat-interval-minutes:5}")
    private int heartbeatIntervalMinutes;

    @Value("${app.sql-server.max-concurrent-jobs:1}")
    private int maxConcurrentJobs;

    @Value("${app.sql-server.prevent-multiple-runs:true}")
    private boolean preventMultipleRuns;

    // Track active jobs to prevent overlaps for 4-hour operations
    private static final AtomicBoolean isJobRunning = new AtomicBoolean(false);
    private static volatile String currentJobId = null;

    // Enhanced load status for 4-hour operations with detailed tracking
    private volatile DataLoadStatusDto currentLoadStatus = new DataLoadStatusDto(
            "IDLE", "No CBC data loading operation in progress", 0L, null, null, null);

    // ============ DATA LOADING OPERATIONS (4-HOUR HANDLING) ============

    @Override
    public DataLoadStatusDto loadCbcData(CbcDataRequestDto request) {
        log.info("🚀 Starting CBC data loading for date range: {} to {} (Expected duration: UP TO 4 HOURS)", 
                request.getStartDate(), request.getEndDate());

        // Check if already loading (critical for 4-hour operations)
        if (preventMultipleRuns && isJobRunning.get()) {
            log.warn("⚠️ CBC data loading already in progress. Current job: {}", currentJobId);
            return new DataLoadStatusDto(
                "BLOCKED", 
                String.format("Another CBC data loading job is already running (Job ID: %s). Please wait for completion.", currentJobId), 
                currentLoadStatus.getTotalRecords(),
                currentLoadStatus.getLastLoadTime(),
                currentLoadStatus.getStartTime(),
                currentLoadStatus.getEndTime()
            );
        }

        // Get current username BEFORE async to avoid PostgreSQL connection issues during 4-hour operation
        String currentUser;
        try {
            currentUser = getCurrentUsername();
            log.info("👤 4-hour CBC job initiated by user: {}", currentUser);
        } catch (Exception e) {
            log.warn("⚠️ Could not determine current user for 4-hour job: {}", e.getMessage());
            currentUser = "SYSTEM";
        }

        // Generate unique job ID for 4-hour tracking
        String jobId = "CBC_4H_JOB_" + System.currentTimeMillis();
        currentJobId = jobId;
        
        log.info("🏷️ Assigned 4-hour job ID: {} for date range {} to {}", jobId, request.getStartDate(), request.getEndDate());
        
        // Start the 4-hour background job (HTTP response returned immediately)
        loadDataAsyncLongRunning(request, currentUser, jobId);
        
        // Return immediate response - job will run in background for up to 4 hours
        return new DataLoadStatusDto(
            "LOADING",
            String.format("CBC stored procedure started (Job ID: %s). Expected duration: 2-4 hours. Monitor status with GET /api/v1/cbc-data/staging/load-status", jobId),
            0L,
            null,
            LocalDateTime.now(),
            null
        );
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> loadDataAsyncLongRunning(CbcDataRequestDto request, String currentUser, String jobId) {
        LocalDateTime startTime = LocalDateTime.now();
        
        // Set job as running (prevent concurrent 4-hour jobs)
        isJobRunning.set(true);
        
        // Create heartbeat executor for progress monitoring during 4-hour execution
        ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CBC-Heartbeat-" + jobId);
            t.setDaemon(true);
            return t;
        });
        
        AtomicLong currentRecordCount = new AtomicLong(0);
        
        try {
            log.info("🔄 Starting 4-hour CBC background job: {}", jobId);
            
            updateLoadStatus("LOADING", 
                String.format("Job %s: Preparing to execute CBC stored procedure (Est. 2-4 hours) for dates %s to %s", 
                             jobId, request.getStartDate(), request.getEndDate()), 
                0L, null, startTime, null);

            // Start heartbeat to update status every 5 minutes during 4-hour execution
            heartbeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    Duration runningDuration = Duration.between(startTime, LocalDateTime.now());
                    updateLoadStatus("LOADING", 
                        String.format("Job %s: CBC stored procedure running... Duration: %d hours %d minutes", 
                                     jobId, runningDuration.toHours(), runningDuration.toMinutes() % 60), 
                        currentRecordCount.get(), null, startTime, null);
                    
                    log.info("💓 Heartbeat - Job {} still running. Duration: {} hours {} minutes, Records processed: {}", 
                            jobId, runningDuration.toHours(), runningDuration.toMinutes() % 60, currentRecordCount.get());
                } catch (Exception e) {
                    log.warn("⚠️ Error in heartbeat for job {}: {}", jobId, e.getMessage());
                }
            }, heartbeatIntervalMinutes, heartbeatIntervalMinutes, TimeUnit.MINUTES);

            // Clear existing staging data first
            log.info("🗑️ Job {}: Clearing existing staging data", jobId);
            stagingRepository.deleteAllStaging();

            // Execute the 4-hour stored procedure
            log.info("⏱️ Job {}: Starting 4-hour stored procedure execution", jobId);
            List<CbcStagingRecordEntity> stagingRecords = loadDataFromSqlServerLongRunning(
                    request.getStartDate().toString(), 
                    request.getEndDate().toString(), 
                    currentUser,
                    jobId,
                    currentRecordCount);
            
            if (stagingRecords.isEmpty()) {
                log.warn("⚠️ Job {}: No records returned from stored procedure for date range {} to {}", 
                        jobId, request.getStartDate(), request.getEndDate());
            }
            
            // Save in batches (this might take additional time for large datasets)
            log.info("💾 Job {}: Saving {} records to staging table", jobId, stagingRecords.size());
            saveInBatchesWithProgress(stagingRecords, jobId, currentRecordCount);
            
            LocalDateTime endTime = LocalDateTime.now();
            Duration totalDuration = Duration.between(startTime, endTime);
            
            updateLoadStatus("SUCCESS", 
                String.format("Job %s: CBC data loaded successfully! Duration: %d hours %d minutes, Records: %d", 
                             jobId, totalDuration.toHours(), totalDuration.toMinutes() % 60, stagingRecords.size()), 
                (long) stagingRecords.size(), endTime, startTime, endTime);
            
            log.info("✅ Job {} completed successfully! Loaded {} records in {} hours {} minutes", 
                    jobId, stagingRecords.size(), totalDuration.toHours(), totalDuration.toMinutes() % 60);
            
        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            Duration failedDuration = Duration.between(startTime, endTime);
            
            log.error("❌ Job {} failed after {} hours {} minutes: {}", 
                     jobId, failedDuration.toHours(), failedDuration.toMinutes() % 60, e.getMessage(), e);
            
            String errorMessage = String.format("Job %s failed after %d hours %d minutes: %s", 
                                               jobId, failedDuration.toHours(), failedDuration.toMinutes() % 60, e.getMessage());
            if (e.getMessage().contains("timeout")) {
                errorMessage = String.format("Job %s timed out after %d hours. Database may still be processing.", 
                                            jobId, failedDuration.toHours());
            } else if (e.getMessage().contains("connection")) {
                errorMessage = String.format("Job %s: Database connection lost after %d hours. Check network/database status.", 
                                            jobId, failedDuration.toHours());
            }
            
            updateLoadStatus("FAILED", errorMessage, currentRecordCount.get(), endTime, startTime, endTime);
            
        } finally {
            // Clean up
            try {
                heartbeatExecutor.shutdown();
                if (!heartbeatExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    heartbeatExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatExecutor.shutdownNow();
            }
            
            isJobRunning.set(false);
            currentJobId = null;
            
            log.info("🧹 Job {} cleanup completed. System ready for next 4-hour job.", jobId);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    private List<CbcStagingRecordEntity> loadDataFromSqlServerLongRunning(String startDate, String endDate, 
                                                                           String currentUser, String jobId,
                                                                           AtomicLong currentRecordCount) 
            throws SQLException, ClassNotFoundException {
        
        log.info("🔗 Job {}: Connecting to SQL Server for 4-hour stored procedure", jobId);
        List<CbcStagingRecordEntity> records = new ArrayList<>();
        
        Class.forName(dbDriver);
        
        Connection connection = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // Create connection with EXTREME timeouts for 4-hour operations
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            
            // Set UNLIMITED timeouts for 4-hour operations
            connection.setNetworkTimeout(null, 0); // 0 = unlimited
            connection.setAutoCommit(false);
            
            log.info("✅ Job {}: SQL Server connection established for long-running operation", jobId);
            
            // Prepare callable statement with UNLIMITED timeout
            String storedProcCall = buildCbcDataQuery();
            stmt = connection.prepareCall(storedProcCall);
            stmt.setQueryTimeout(0); // 0 = unlimited timeout for 4-hour operation
            
            // Set date parameters
            Date sqlStartDate = Date.valueOf(startDate);
            Date sqlEndDate = Date.valueOf(endDate);
            
            stmt.setDate(1, sqlStartDate);
            stmt.setDate(2, sqlEndDate);
            
            log.info("🚀 Job {}: Executing stored procedure with UNLIMITED timeout", jobId);
            log.info("📅 Job {}: Parameters - FROMDATE: {}, TODATE: {}", jobId, startDate, endDate);
            
            long executionStart = System.currentTimeMillis();
            
            // Execute the 4-hour stored procedure
            boolean hasResultSet = stmt.execute();
            
            long executionTime = System.currentTimeMillis() - executionStart;
            log.info("⏱️ Job {}: Stored procedure completed in {} minutes ({} seconds)", 
                    jobId, executionTime / 60000, executionTime / 1000);
            
            if (hasResultSet) {
                rs = stmt.getResultSet();
                log.info("📊 Job {}: Processing result set from stored procedure", jobId);
                
                long lastLogTime = System.currentTimeMillis();
                int recordCount = 0;
                
                while (rs.next()) {
                    try {
                        CbcStagingRecordEntity entity = resultSetMapper.mapResultSetToStagingEntity(rs, currentUser);
                        records.add(entity);
                        recordCount++;
                        currentRecordCount.incrementAndGet();
                        
                        // Log progress every 10 minutes during result processing
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastLogTime > (progressLogIntervalMinutes * 60 * 1000)) {
                            log.info("📈 Job {}: Processed {} records so far from stored procedure result set", 
                                    jobId, recordCount);
                            lastLogTime = currentTime;
                        }
                        
                        // Yield thread occasionally for very large datasets (memory management)
                        if (recordCount % 10000 == 0) {
                            Thread.yield();
                        }
                        
                    } catch (Exception e) {
                        log.warn("⚠️ Job {}: Error processing record at position {}: {}", 
                                jobId, recordCount + 1, e.getMessage());
                        // Continue processing other records instead of failing entire 4-hour job
                    }
                }
                
                log.info("✅ Job {}: Finished processing result set. Total records: {}", jobId, recordCount);
            } else {
                log.warn("⚠️ Job {}: Stored procedure did not return a result set", jobId);
            }
            
            // Commit the transaction
            connection.commit();
            log.info("✅ Job {}: SQL Server transaction committed successfully", jobId);
            
        } catch (SQLException e) {
            log.error("❌ Job {}: SQL error during stored procedure execution: {}", jobId, e.getMessage());
            
            // Rollback on error
            if (connection != null) {
                try {
                    connection.rollback();
                    log.info("🔄 Job {}: SQL Server transaction rolled back", jobId);
                } catch (SQLException rollbackEx) {
                    log.error("❌ Job {}: Error during rollback: {}", jobId, rollbackEx.getMessage());
                }
            }
            
            // Enhanced error messages for 4-hour operations
            if (e.getMessage().contains("timeout")) {
                throw new SQLException(String.format("Job %s: Stored procedure execution timed out after 4+ hours. Database may still be processing.", jobId), e);
            } else if (e.getMessage().contains("permission") || e.getMessage().contains("denied")) {
                throw new SQLException(String.format("Job %s: Insufficient permissions to execute stored procedure: %s", jobId, e.getMessage()), e);
            } else if (e.getMessage().contains("deadlock")) {
                throw new SQLException(String.format("Job %s: Database deadlock detected during 4-hour operation. Please retry.", jobId), e);
            } else if (e.getMessage().contains("connection")) {
                throw new SQLException(String.format("Job %s: Database connection lost during 4-hour operation: %s", jobId, e.getMessage()), e);
            } else {
                throw new SQLException(String.format("Job %s: %s", jobId, e.getMessage()), e);
            }
        } finally {
            // Clean up resources in reverse order
            if (rs != null) {
                try {
                    rs.close();
                    log.debug("Job {}: ResultSet closed", jobId);
                } catch (SQLException e) {
                    log.warn("Job {}: Error closing ResultSet: {}", jobId, e.getMessage());
                }
            }
            
            if (stmt != null) {
                try {
                    stmt.close();
                    log.debug("Job {}: CallableStatement closed", jobId);
                } catch (SQLException e) {
                    log.warn("Job {}: Error closing CallableStatement: {}", jobId, e.getMessage());
                }
            }
            
            if (connection != null) {
                try {
                    connection.close();
                    log.info("🔗 Job {}: SQL Server connection closed", jobId);
                } catch (SQLException e) {
                    log.warn("Job {}: Error closing SQL Server connection: {}", jobId, e.getMessage());
                }
            }
        }
        
        log.info("✅ Job {}: Successfully loaded {} records from 4-hour stored procedure", jobId, records.size());
        return records;
    }

    private void saveInBatchesWithProgress(List<CbcStagingRecordEntity> records, String jobId, AtomicLong currentRecordCount) {
        log.info("💾 Job {}: Starting to save {} records in batches of {}", jobId, records.size(), batchSize);
        
        for (int i = 0; i < records.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, records.size());
            List<CbcStagingRecordEntity> batch = records.subList(i, endIndex);
            
            try {
                stagingRepository.saveAll(batch);
                log.info("💾 Job {}: Saved batch {}/{} - {} records", 
                        jobId,
                        (i / batchSize) + 1, 
                        (records.size() + batchSize - 1) / batchSize, 
                        batch.size());
            } catch (Exception e) {
                log.error("❌ Job {}: Error saving batch starting at index {}: {}", jobId, i, e.getMessage());
                throw new RuntimeException(String.format("Job %s: Failed to save records batch", jobId), e);
            }
        }
        
        log.info("✅ Job {}: All {} records saved successfully to staging table", jobId, records.size());
    }

    @Override
    public DataLoadStatusDto getLoadStatus() {
        return currentLoadStatus;
    }

    @Override
    @Transactional
    public void clearStagingDataSync() {
        log.info("🗑️ Clearing all staging data synchronously");
        stagingRepository.deleteAllStaging();
        log.info("✅ Staging data cleared successfully");
    }

    // ============ RECORD RETRIEVAL OPERATIONS ============

    @Override
    public PaginationResponse<CbcRecordResponseDto> getStagingRecordsPaginated(CbcFilterRequestDto filterRequest) {
        log.info("📄 Fetching staging records with pagination. Page: {}, Size: {}", 
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
        log.info("📄 Fetching all staging records without pagination");

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
        log.info("🔍 Fetching staging record by ID: {}", id);
        CbcStagingRecordEntity entity = stagingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staging record not found with ID: " + id));
        return cbcMapper.stagingToResponseDto(entity);
    }

    @Override
    public List<CbcRecordResponseDto> getStagingRecordsByIds(List<UUID> ids) {
        log.info("🔍 Fetching staging records by IDs: {}", ids);
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
        log.info("✏️ Updating staging record with ID: {}", id);
        
        String currentUser = getCurrentUsername();
        
        CbcStagingRecordEntity entity = stagingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staging record not found with ID: " + id));

        // Update using mapper
        cbcMapper.updateStagingFromDto(updateRequest, entity);
        entity.setUpdatedBy(currentUser);

        CbcStagingRecordEntity savedEntity = stagingRepository.save(entity);
        log.info("✅ Staging record updated successfully with ID: {}", id);
        
        return cbcMapper.stagingToResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public List<CbcRecordResponseDto> updateMultipleStagingRecords(CbcBulkUpdateRequestDto bulkUpdateRequest) {
        log.info("✏️ Processing bulk update for {} staging records", bulkUpdateRequest.getUpdates().size());
        
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

        log.info("✅ Bulk update completed. Updated {} staging records with {} errors", 
                updatedRecords.size(), errors.size());
        
        return updatedRecords;
    }

    // ============ VALIDATION OPERATIONS ============

    @Override
    @Transactional
    public CbcRecordResponseDto validateStagingRecord(UUID id) {
        log.info("🔍 Validating staging record with ID: {}", id);
        
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
        log.info("🔍 Validating {} staging records", ids.size());
        
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
        log.info("🔍 Searching staging records by account number: {}", accountNumber);
        List<CbcStagingRecordEntity> entities = stagingRepository.findByAccountNumber(accountNumber);
        return cbcMapper.stagingListToResponseDtoList(entities);
    }

    @Override
    public List<CbcRecordResponseDto> searchByCreditorId(String creditorId) {
        log.info("🔍 Searching staging records by creditor ID: {}", creditorId);
        List<CbcStagingRecordEntity> entities = stagingRepository.findByCreditorId(creditorId);
        return cbcMapper.stagingListToResponseDtoList(entities);
    }

    @Override
    public List<CbcRecordResponseDto> searchByValidationStatus(String status) {
        log.info("🔍 Searching staging records by validation status: {}", status);
        List<CbcStagingRecordEntity> entities = stagingRepository.findByValidationStatus(status);
        return cbcMapper.stagingListToResponseDtoList(entities);
    }

    // ============ PRIVATE HELPER METHODS ============

    private String getCurrentUsername() {
        try {
            UserEntity user = securityUtils.getCurrentUser();
            return user.getUsername();
        } catch (Exception e) {
            log.warn("Could not get current user (possibly async context): {}", e.getMessage());
            
            // Try to get from Spring Security context directly
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    return auth.getName();
                }
            } catch (Exception secEx) {
                log.warn("Could not get user from Security context: {}", secEx.getMessage());
            }
            
            // Fallback to system user
            return "SYSTEM";
        }
    }

    private void updateLoadStatus(String status, String message, Long totalRecords, 
                                LocalDateTime lastLoadTime, LocalDateTime startTime, LocalDateTime endTime) {
        this.currentLoadStatus = new DataLoadStatusDto(status, message, totalRecords, 
                lastLoadTime, startTime, endTime);
    }

    private String buildCbcDataQuery() {
        // Call the stored procedure that generates CBC data (4-hour execution)
        return "{CALL dbo.RP_CBC_UPLOAD_MONTHLY_STORE(?, ?)}";
t ad    }

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

    // ============ UTILITY METHODS FOR 4-HOUR OPERATIONS ============

    public boolean isJobCurrentlyRunning() {
        return isJobRunning.get();
    }

    public String getCurrentJobId() {
        return currentJobId;
    }

    public String getJobStatusSummary() {
        if (isJobRunning.get()) {
            Duration runningTime = Duration.between(
                currentLoadStatus.getStartTime() != null ? currentLoadStatus.getStartTime() : LocalDateTime.now(),
                LocalDateTime.now()
            );
            return String.format("Job %s running for %d hours %d minutes. Records processed: %d", 
                    currentJobId, 
                    runningTime.toHours(), 
                    runningTime.toMinutes() % 60,
                    currentLoadStatus.getTotalRecords() != null ? currentLoadStatus.getTotalRecords() : 0);
        } else {
            return "No CBC job currently running";
        }
    }
}