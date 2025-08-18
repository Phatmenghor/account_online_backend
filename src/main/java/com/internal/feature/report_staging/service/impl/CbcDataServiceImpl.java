package com.internal.feature.report_staging.service.impl;

import com.internal.exceptions.error.NotFoundException;
import com.internal.exceptions.error.BadRequestException;
import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.response.BatchSessionResponseDto;
import com.internal.feature.report_staging.dto.response.CbcRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.mapper.CbcMapper;
import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import com.internal.feature.report_staging.models.CbcFinalRecordEntity;
import com.internal.feature.report_staging.repository.CbcStagingRepository;
import com.internal.feature.report_staging.repository.CbcFinalRepository;
import com.internal.feature.report_staging.service.CbcDataService;
import com.internal.feature.report_staging.specification.CbcFinalRecordSpecification;
import com.internal.utils.SecurityUtils;
import com.internal.utils.connnection.JdbcInternalConnection;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbcDataServiceImpl implements CbcDataService {

    private final CbcStagingRepository stagingRepository;
    private final CbcFinalRepository finalRepository;
    private final CbcMapper cbcMapper;
    private final SecurityUtils securityUtils;
    private final JdbcInternalConnection jdbcConnection;

    private volatile DataLoadStatusDto currentLoadStatus = new DataLoadStatusDto(
            "IDLE", "No data loading operation in progress", 0L, null, null, null);

    private static final String STORED_PROCEDURE_NAME = "RP_CBC_UPLOAD_MONTHLY_STORE";
    private static final int BATCH_SIZE = 1000;

    // ============ STAGING OPERATIONS ============

    @Override
    public DataLoadStatusDto loadCbcData(CbcDataRequestDto request) {
        log.info("Starting CBC data load operation for date range: {} to {}", 
                request.getStartDate(), request.getEndDate());

        validateDateRange(request);
        String currentUser = getCurrentUsername();
        LocalDateTime startTime = LocalDateTime.now();

        // Update status to loading
        currentLoadStatus = createLoadingStatus(startTime);

        // Execute data loading asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                clearStagingDataSync();
                long recordCount = loadDataFromSqlServer(request, currentUser);
                
                LocalDateTime endTime = LocalDateTime.now();
                currentLoadStatus = new DataLoadStatusDto(
                        "SUCCESS", 
                        String.format("Data loading completed successfully. Loaded %d records into staging", recordCount), 
                        recordCount, endTime, startTime, endTime);

                log.info("CBC data loading completed successfully. Total records in staging: {}", recordCount);

            } catch (Exception e) {
                log.error("Error during data loading: {}", e.getMessage(), e);
                LocalDateTime endTime = LocalDateTime.now();
                currentLoadStatus = new DataLoadStatusDto(
                        "FAILED", 
                        "Data loading failed: " + e.getMessage(), 
                        0L, null, startTime, endTime);
            }
        });

        return currentLoadStatus;
    }

    @Override
    public DataLoadStatusDto getLoadStatus() {
        return currentLoadStatus;
    }

    @Override
    public PaginationResponse<CbcRecordResponseDto> getStagingRecordsPaginated(CbcFilterRequestDto filterRequest) {
        log.info("Fetching staging records with pagination. Page: {}, Size: {}", 
                filterRequest.getPage(), filterRequest.getSize());

        // Create specification using clean code approach
        Specification<CbcStagingRecordEntity> spec = CbcRecordResponseDto.CbcStagingRecordSpecification.withFilters(
                filterRequest.getSearch()
        );

        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                          filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage() - 1, filterRequest.getSize(), sort);

        // Fetch data using specification
        Page<CbcStagingRecordEntity> page = stagingRepository.findAll(spec, pageable);

        // Convert to response using mapper
        List<CbcRecordResponseDto> content = page.getContent().stream()
                .map(cbcMapper::stagingToResponseDto)
                .collect(Collectors.toList());

        return createPaginationResponse(content, page, filterRequest);
    }

    @Override
    public CbcRecordResponseDto getStagingRecordById(Long id) {
        log.info("Fetching staging record by ID: {}", id);
        CbcStagingRecordEntity entity = stagingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staging record not found with ID: " + id));
        return cbcMapper.stagingToResponseDto(entity);
    }

    @Override
    @Transactional
    public CbcRecordResponseDto updateStagingRecord(Long id, CbcUpdateRequestDto updateRequest) {
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
    public long getStagingRecordsCount() {
        return stagingRepository.countAllStaging();
    }

    @Override
    @Transactional
    public void clearStagingDataSync() {
        try {
            log.info("Clearing existing staging data");
            stagingRepository.deleteAllStaging();
            log.info("Staging data cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing staging data: {}", e.getMessage());
            throw new RuntimeException("Failed to clear staging data", e);
        }
    }

    // ============ FINAL/HISTORY OPERATIONS ============

    @Override
    @Transactional
    public String moveStagingToFinal() {
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

        // Save to final table
        finalRepository.saveAll(finalRecords);
        
        // Clear staging table
        stagingRepository.deleteAllStaging();
        
        log.info("Successfully moved {} records to final table with batch session: {}", 
                finalRecords.size(), batchSessionId);
        
        return batchSessionId;
    }

    @Override
    public PaginationResponse<CbcRecordResponseDto> getFinalRecordsPaginated(CbcFilterRequestDto filterRequest) {
        log.info("Fetching final records with pagination. Page: {}, Size: {}", 
                filterRequest.getPage(), filterRequest.getSize());

        // Create specification using clean code approach
        Specification<CbcFinalRecordEntity> spec = CbcFinalRecordSpecification.withFilters(
                filterRequest.getStartDate(),
                filterRequest.getEndDate(),
                filterRequest.getBatchSessionId(),
                filterRequest.getSearch()
        );

        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                          filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage() - 1, filterRequest.getSize(), sort);

        // Fetch data using specification
        Page<CbcFinalRecordEntity> page = finalRepository.findAll(spec, pageable);

        // Convert to response using mapper
        List<CbcRecordResponseDto> content = page.getContent().stream()
                .map(cbcMapper::finalToResponseDto)
                .collect(Collectors.toList());

        return createPaginationResponse(content, page, filterRequest);
    }

    @Override
    public List<BatchSessionResponseDto> getAllBatchSessions() {
        log.info("Fetching all batch sessions");
        
        List<Object[]> results = finalRepository.findAllBatchSessionsSummary();
        
        return results.stream()
                .map(cbcMapper::mapToBatchSessionDto)
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
                .map(cbcMapper::mapToBatchSessionDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getFinalRecordsCount() {
        return finalRepository.countAllFinal();
    }

    // ============ PRIVATE HELPER METHODS ============

    private void validateDateRange(CbcDataRequestDto request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        
        if (request.getStartDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the future");
        }
    }

    private String getCurrentUsername() {
        try {
            return securityUtils.getCurrentUser().getUsername();
        } catch (Exception e) {
            log.warn("Could not get current user, using system user: {}", e.getMessage());
            return "SYSTEM";
        }
    }

    private DataLoadStatusDto createLoadingStatus(LocalDateTime startTime) {
        return new DataLoadStatusDto(
                "LOADING", "Data loading in progress...", 0L, null, startTime, null);
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
        
        // Copy all fields from staging to final
        BeanUtils.copyProperties(staging, finalRecord, "id", "createdAt", "updatedAt");
        
        // Set batch session info
        finalRecord.setBatchSessionId(batchSessionId);
        finalRecord.setBatchSessionDate(batchDate);
        finalRecord.setProcessedDate(processedTime);
        finalRecord.setOriginalLoadDate(staging.getCreatedAt() != null ? 
                staging.getCreatedAt().toLocalDate() : LocalDate.now());
        finalRecord.setWasUpdated(staging.getIsUpdated());
        
        finalRecord.setCreatedBy(currentUser);
        finalRecord.setUpdatedBy(currentUser);
        
        return finalRecord;
    }

    private long loadDataFromSqlServer(CbcDataRequestDto request, String currentUser) {
        Connection connection = null;
        CallableStatement callableStatement = null;
        ResultSet resultSet = null;

        try {
            log.info("Connecting to SQL Server database");
            connection = jdbcConnection.getConnection();

            String sql = String.format("{call %s(?, ?)}", STORED_PROCEDURE_NAME);
            callableStatement = connection.prepareCall(sql);
            callableStatement.setDate(1, Date.valueOf(request.getStartDate()));
            callableStatement.setDate(2, Date.valueOf(request.getEndDate()));

            log.info("Executing stored procedure {} with parameters: {}, {}", 
                    STORED_PROCEDURE_NAME, request.getStartDate(), request.getEndDate());

            resultSet = callableStatement.executeQuery();

            return processResultSetToStaging(resultSet, currentUser);

        } catch (SQLException e) {
            log.error("SQL error during data loading - Code: {}, State: {}, Message: {}", 
                    e.getErrorCode(), e.getSQLState(), e.getMessage());
            throw new RuntimeException("Failed to load CBC data from SQL Server: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error loading CBC data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load CBC data", e);
        } finally {
            jdbcConnection.cleanup(connection, callableStatement, resultSet);
        }
    }

    private long processResultSetToStaging(ResultSet resultSet, String currentUser) throws SQLException {
        List<CbcStagingRecordEntity> entities = new ArrayList<>();
        long recordCount = 0;

        while (resultSet.next()) {
            try {
                CbcStagingRecordEntity stagingRecord = mapResultSetToStagingEntity(resultSet, currentUser);
                entities.add(stagingRecord);
                recordCount++;

                if (entities.size() >= BATCH_SIZE) {
                    saveStagingBatch(entities, recordCount);
                    entities.clear();
                }
            } catch (Exception e) {
                log.warn("Error processing record {}: {}", recordCount + 1, e.getMessage());
            }
        }

        // Save remaining records
        if (!entities.isEmpty()) {
            saveStagingBatch(entities, recordCount);
        }

        return recordCount;
    }

    @Transactional
    public void saveStagingBatch(List<CbcStagingRecordEntity> entities, long totalProcessed) {
        try {
            stagingRepository.saveAll(entities);
            log.info("Saved staging batch of {} records. Total processed: {}", entities.size(), totalProcessed);
        } catch (Exception e) {
            log.error("Error saving staging batch: {}", e.getMessage());
            throw new RuntimeException("Failed to save staging batch", e);
        }
    }

    private CbcStagingRecordEntity mapResultSetToStagingEntity(ResultSet rs, String currentUser) throws SQLException {
        CbcStagingRecordEntity entity = new CbcStagingRecordEntity();
        
        // Main Record Fields
        entity.setAccountNumber(rs.getString("Account Number"));
        entity.setCreditorId(rs.getString("CreditorID"));
        entity.setAccountType(rs.getString("AccountType"));
        entity.setAsOfDate(rs.getString("AsofDate"));
        
        // Personal Info Fields
        entity.setDateOfBirth(rs.getString("DateofBirth"));
        entity.setFamilyNameEnglish(rs.getString("FamilyName(English)"));
        entity.setFirstNameEnglish(rs.getString("FirstName(English)"));
        entity.setSecondNameEnglish(rs.getString("SecondName(English)"));
        entity.setThirdNameEnglish(rs.getString("ThirdName(English)"));
        entity.setUnformattedNameEnglish(rs.getString("UnformattedName(English)"));
        entity.setMothersNameUnformattedEnglish(rs.getString("MothersNameUnformatted(English)"));
        entity.setFamilyNameKhmer(rs.getString("FamilyName(Khmer)"));
        entity.setFirstNameKhmer(rs.getString("FirstName(Khmer)"));
        entity.setSecondNameKhmer(rs.getString("SecondName(Khmer)"));
        entity.setThirdNameKhmer(rs.getString("ThirdName(Khmer)"));
        entity.setUnformattedNameKhmer(rs.getString("UnformattedName(Khmer)"));
        entity.setMothersNameUnformattedKhmer(rs.getString("MothersNameUnformatted(Khmer)"));
        entity.setGender(rs.getString("Gender"));
        entity.setMaritalStatus(rs.getString("MaritalStatus"));
        entity.setNationalityCode(rs.getString("NationalityCode"));
        entity.setTaxpayerRegistrationNumber(rs.getString("TaxpayerRegistrationNumber"));
        entity.setApplicantType(rs.getString("ApplicantType"));
        
        // ID Information Fields
        entity.setIdType1(rs.getString("IDType-1"));
        entity.setIdNumber1(rs.getString("IDNumber-1"));
        entity.setIdExpiryDate1(rs.getString("IDExpiryDate-1"));
        entity.setIdType2(rs.getString("IDType-2"));
        entity.setIdNumber2(rs.getString("IDNumber-2"));
        entity.setIdExpiryDate2(rs.getString("IDExpiryDate-2"));
        entity.setIdType3(rs.getString("IDType-3"));
        entity.setIdNumber3(rs.getString("IDNumber-3"));
        entity.setIdExpiryDate3(rs.getString("IDExpiryDate-3"));
        
        // Address Information Fields - Address 1
        entity.setAddressType1(rs.getString("AddressType-1"));
        entity.setProvince1(rs.getString("Province-1"));
        entity.setDistrict1(rs.getString("District-1"));
        entity.setCommune1(rs.getString("Commune-1"));
        entity.setVillage1(rs.getString("Village-1"));
        entity.setAddress1Field1English(rs.getString("Address-1Field1(English)"));
        entity.setAddress1Field2English(rs.getString("Address-1Field2(English)"));
        entity.setAddress1Field1Khmer(rs.getString("Address-1Field1(Khmer)"));
        entity.setAddress1Field2Khmer(rs.getString("Address-1Field2(Khmer)"));
        entity.setCity1English(rs.getString("City-1(English)"));
        entity.setCity1Khmer(rs.getString("City-1(Khmer)"));
        entity.setCountry1(rs.getString("Country-1"));
        entity.setPostalCode1(rs.getString("PostalCode-1"));
        
        // Address 2
        entity.setAddressType2(rs.getString("AddressType-2"));
        entity.setProvince2(rs.getString("Province-2"));
        entity.setDistrict2(rs.getString("District-2"));
        entity.setCommune2(rs.getString("Commune-2"));
        entity.setVillage2(rs.getString("Village-2"));
        entity.setAddress2Field1English(rs.getString("Address-2Field1(English)"));
        entity.setAddress2Field2English(rs.getString("Address-2Field2(English)"));
        entity.setAddress2Field1Khmer(rs.getString("Address-2Field1(Khmer)"));
        entity.setAddress2Field2Khmer(rs.getString("Address-2Field2(Khmer)"));
        entity.setCity2English(rs.getString("City-2(English)"));
        entity.setCity2Khmer(rs.getString("City-2(Khmer)"));
        entity.setCountry2(rs.getString("Country-2"));
        entity.setPostalCode2(rs.getString("PostalCode-2"));
        
        // Address 3
        entity.setAddress3Type(rs.getString("Address-3Type"));
        entity.setProvince3(rs.getString("Province-3"));
        entity.setDistrict3(rs.getString("District-3"));
        entity.setCommune3(rs.getString("Commune-3"));
        entity.setVillage3(rs.getString("Village-3"));
        entity.setAddress3Field1English(rs.getString("Address-3Field1(English)"));
        entity.setAddress3Field2English(rs.getString("Address-3Field2(English)"));
        entity.setAddress3Field1Khmer(rs.getString("Address-3Field1(Khmer)"));
        entity.setAddress3Field2Khmer(rs.getString("Address-3Field2(Khmer)"));
        entity.setCity3English(rs.getString("City-3(English)"));
        entity.setCity3Khmer(rs.getString("City-3(Khmer)"));
        entity.setCountry3(rs.getString("Country-3"));
        entity.setPostalCode3(rs.getString("PostalCode-3"));
        
        // Contact Information Fields
        entity.setEmailAddress(rs.getString("EmailAddress"));
        entity.setContactNumberType1(rs.getString("ContactNumberType1"));
        entity.setContactNumberCountryCode1(rs.getString("ContactNumber–CountryCode1"));
        entity.setContactNumberArea1(rs.getString("ContactNumber–Area1"));
        entity.setContactNumberNumber1(rs.getString("ContactNumber–Number1"));
        entity.setContactNumberExtension1(rs.getString("ContactNumber–Extension1"));
        entity.setContactNumberType2(rs.getString("ContactNumberType2"));
        entity.setContactNumberCountryCode2(rs.getString("ContactNumber–CountryCode2"));
        entity.setContactNumberArea2(rs.getString("ContactNumber–Area2"));
        entity.setContactNumberNumber2(rs.getString("ContactNumber–Number2"));
        entity.setContactNumberExtension2(rs.getString("ContactNumber–Extension2"));
        entity.setContactNumberType3(rs.getString("ContactNumberType3"));
        entity.setContactNumberCountryCode3(rs.getString("ContactNumber–CountryCode3"));
        entity.setContactNumberArea3(rs.getString("ContactNumber–Area3"));
        entity.setContactNumberNumber3(rs.getString("ContactNumber–Number3"));
        entity.setContactNumberExtension3(rs.getString("ContactNumber–Extension3"));
        
        // Employment Information Fields - Employment 1
        entity.setEmploymentStatus1(rs.getString("Employment Status-1"));
        entity.setEmploymentType1(rs.getString("Employment Type-1"));
        entity.setEmployer1NameEnglish(rs.getString("Employer-1Name(English)"));
        entity.setEmployer1NameKhmer(rs.getString("Employer-1Name(Khmer)"));
        entity.setEconomicSector1(rs.getString("EconomicSector-1"));
        entity.setBusinessType1(rs.getString("BusinessType-1"));
        entity.setEmployer1AddressEnglish(rs.getString("Employer-1'sAddress(English)"));
        entity.setEmployer1AddressKhmer(rs.getString("Employer-1'sAddress(Khmer)"));
        entity.setEmployer1Province(rs.getString("Employer-1'sProvince"));
        entity.setEmployer1District(rs.getString("Employer-1'sDistrict"));
        entity.setEmployer1Commune(rs.getString("Employer-1'sCommune"));
        entity.setEmployer1Village(rs.getString("Employer-1'sVillage"));
        entity.setEmployer1AddressCityEnglish(rs.getString("Employer-1'sAddressCity(English)"));
        entity.setEmployer1AddressCityKhmer(rs.getString("Employer-1'sAddressCity(Khmer)"));
        entity.setEmp1Country1(rs.getString("EMP1Country-1"));
        entity.setEmp1PostalCode1(rs.getString("EMP1PostalCode-1"));
        entity.setOccupation1English(rs.getString("Occupation-1(English)"));
        entity.setOccupation1Khmer(rs.getString("Occupation-1(Khmer)"));
        entity.setDateOfEmployment1(rs.getString("DateofEmployment-1"));
        entity.setLengthOfService1Months(rs.getString("LengthofService-1(Months)"));
        entity.setContractExpiryDate1(rs.getString("ContractExpiryDate-1"));
        entity.setCurrency1(rs.getString("Currency-1"));
        entity.setMonthlyBasicSalaryIncome1(getBigDecimalFromResultSet(rs, "MonthlyBasicSalary/Income-1"));
        entity.setTotalMonthlySalaryIncome1(getBigDecimalFromResultSet(rs, "TotalMonthlySalary/Income-1"));
        
        // Employment 2
        entity.setEmployerType2(rs.getString("Employer-2Type"));
        entity.setSelfEmployed2(rs.getString("SelfEmployed-2"));
        entity.setEmployer2NameEnglish(rs.getString("Employer-2Name(English)"));
        entity.setEmployer2NameKhmer(rs.getString("Employer-2Name(Khmer)"));
        entity.setEconomicSector2(rs.getString("EconomicSector-2"));
        entity.setBusinessType2(rs.getString("BusinessType-2"));
        entity.setEmployer2AddressEnglish(rs.getString("Employer-2'sAddress(English)"));
        entity.setEmployer2AddressKhmer(rs.getString("Employer-2'sAddress(Khmer)"));
        entity.setEmployer2Province(rs.getString("Employer-2'sProvince"));
        entity.setEmployer2District(rs.getString("Employer-2'sDistrict"));
        entity.setEmployer2Commune(rs.getString("Employer-2'sCommune"));
        entity.setEmployer2Village(rs.getString("Employer-2'sVillage"));
        entity.setEmployer2AddressCityEnglish(rs.getString("Employer-2'sAddressCity(English)"));
        entity.setEmployer2AddressCityKhmer(rs.getString("Employer-2'sAddressCity(Khmer)"));
        entity.setEmzCountry2(rs.getString("EMZCountry-2"));
        entity.setEmzPostalCode2(rs.getString("EMZPostalCode-2"));
        entity.setOccupation2English(rs.getString("Occupation-2(English)"));
        entity.setOccupation2Khmer(rs.getString("Occupation-2(Khmer)"));
        entity.setDateOfEmployment2(rs.getString("DateofEmployment-2"));
        entity.setLengthOfService2Months(rs.getString("LengthofService-2(Months)"));
        entity.setContractExpiryDate2(rs.getString("ContractExpiryDate-2"));
        entity.setCurrency2(rs.getString("Currency–2"));
        entity.setMonthlyBasicSalaryIncome2(getBigDecimalFromResultSet(rs, "MonthlyBasicSalary/Income-2"));
        entity.setTotalMonthlySalaryIncome2(getBigDecimalFromResultSet(rs, "TotalMonthlySalary/Income-2"));
        
        // Employment 3
        entity.setEmployerType3(rs.getString("EmployerType-3"));
        entity.setSelfEmployed3(rs.getString("SelfEmployed-3"));
        entity.setEmployer3NameEnglish(rs.getString("Employer-3Name(English)"));
        
        // Security Information Fields
        entity.setSecurityType1(rs.getString("Security Type-1"));
        entity.setSecurityNumber1(rs.getString("Security Number-1"));
        entity.setSecurityCurrency1(rs.getString("Security Currency-1"));
        entity.setSecurityValue1(getBigDecimalFromResultSet(rs, "Security Value-1"));
        entity.setSecurityLocation1(rs.getString("Security Location-1"));
        entity.setSecurityType2(rs.getString("Security Type-2"));
        entity.setSecurityNumber2(rs.getString("Security Number-2"));
        entity.setSecurityCurrency2(rs.getString("Security Currency-2"));
        entity.setSecurityValue2(getBigDecimalFromResultSet(rs, "Security Value-2"));
        entity.setSecurityLocation2(rs.getString("Security Location-2"));
        entity.setSecurityType3(rs.getString("Security Type-3"));
        entity.setSecurityNumber3(rs.getString("Security Number-3"));
        entity.setSecurityCurrency3(rs.getString("Security Currency-3"));
        entity.setSecurityValue3(getBigDecimalFromResultSet(rs, "Security Value-3"));
        entity.setSecurityLocation3(rs.getString("Security Location-3"));
        entity.setSecurityTypePrimary(rs.getString("SecurityType-Primary"));
        entity.setSpecialNote(rs.getString("Special Note"));
        entity.setEnquiryMemberReference(rs.getString("Enquiry Member Reference"));
        entity.setLoanToSectorSection(rs.getString("Loan to sector/Section"));
        entity.setCurrency3(rs.getString("Currency-3"));
        entity.setBranchAddressCode(rs.getString("Branch Address Code"));
        
        // Loan Information Fields
        entity.setLoanTermType(rs.getString("Loan Term Type"));
        entity.setGroupAccountReference(rs.getString("Group Account Reference"));
        entity.setDateIssued(rs.getString("Date Issued"));
        entity.setProductType(rs.getString("ProductType"));
        entity.setCurrency(rs.getString("Currency"));
        entity.setProductLimitOriginalAmount(getBigDecimalFromResultSet(rs, "ProductLimit/OriginalAmount"));
        entity.setProductExpiryDate(rs.getString("ProductExpiryDate"));
        entity.setProductStatus(rs.getString("ProductStatus"));
        entity.setRestructuredLoan(rs.getString("Restructured Loan"));
        entity.setInstalmentAmount(getBigDecimalFromResultSet(rs, "InstalmentAmount"));
        entity.setPaymentFrequency(rs.getString("PaymentFrequency"));
        entity.setTenure(rs.getString("Tenure"));
        entity.setLastPaymentDate(rs.getString("LastPaymentDate"));
        entity.setLastAmountPaid(getBigDecimalFromResultSet(rs, "LastAmountPaid"));
        entity.setOutstandingBalance(getBigDecimalFromResultSet(rs, "OutstandingBalance"));
        entity.setPastDue(getBigDecimalFromResultSet(rs, "PastDue"));
        entity.setNextPaymentDate(rs.getString("NextPaymentDate"));
        entity.setPaymentStatusCode(rs.getString("PaymentStatusCode"));
        entity.setLossStatus(rs.getString("LossStatus"));
        entity.setLossStatusDate(rs.getString("LossStatusDate"));
        entity.setOriginalAmountAsAtLoadDate(rs.getString("OriginalAmountasatLoadDate"));
        entity.setEmzOutstandingBalance(rs.getString("EMZOutstandingBalance"));
        
        // Set audit fields
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setIsUpdated(false);
        
        return entity;
    }

    private BigDecimal getBigDecimalFromResultSet(ResultSet rs, String columnName) {
        try {
            Object value = rs.getObject(columnName);
            if (value == null) {
                return null;
            }
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            log.warn("Error converting column {} to BigDecimal: {}", columnName, e.getMessage());
            return null;
        }
    }
}