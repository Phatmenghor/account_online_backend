package com.internal.feature.report_staging.service.impl;

import com.internal.exceptions.error.NotFoundException;
import com.internal.exceptions.error.BadRequestException;
import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.response.CbcMainRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.mapper.CbcMapper;
import com.internal.feature.report_staging.models.*;
import com.internal.feature.report_staging.repository.CbcMainRecordRepository;
import com.internal.feature.report_staging.service.CbcDataService;
import com.internal.feature.report_staging.specification.CbcMainRecordSpecification;
import com.internal.utils.SecurityUtils;
import com.internal.utils.connnection.JdbcInternalConnection;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbcDataServiceImpl implements CbcDataService {

    private final CbcMainRecordRepository mainRecordRepository;
    private final CbcMapper cbcMapper;
    private final SecurityUtils securityUtils;
    private final JdbcInternalConnection jdbcConnection;

    private volatile DataLoadStatusDto currentLoadStatus = new DataLoadStatusDto(
            "IDLE", "No data loading operation in progress", 0L, null, null, null);

    private static final String STORED_PROCEDURE_NAME = "RP_CBC_UPLOAD_MONTHLY_STORE";
    private static final int BATCH_SIZE = 1000;

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
                // Clear existing data first
                clearExistingDataSync();
                
                // Then load new data
                long recordCount = loadDataFromSqlServer(request, currentUser);
                
                LocalDateTime endTime = LocalDateTime.now();
                currentLoadStatus = new DataLoadStatusDto(
                        "SUCCESS", 
                        String.format("Data loading completed successfully. Processed %d records", recordCount), 
                        recordCount, endTime, startTime, endTime);

                log.info("CBC data loading completed successfully. Total records: {}", recordCount);

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

    @Transactional
    public void clearExistingDataSync() {
        try {
            log.info("Clearing existing CBC data");
            mainRecordRepository.deleteAllData();
            log.info("Existing CBC data cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing existing data: {}", e.getMessage());
            throw new RuntimeException("Failed to clear existing data", e);
        }
    }

    private long loadDataFromSqlServer(CbcDataRequestDto request, String currentUser) {
        Connection connection = null;
        CallableStatement callableStatement = null;
        ResultSet resultSet = null;

        try {
            log.info("Connecting to SQL Server database");
            connection = jdbcConnection.getConnection();

            // Call stored procedure with correct name
            String sql = String.format("{call %s(?, ?)}", STORED_PROCEDURE_NAME);
            callableStatement = connection.prepareCall(sql);
            callableStatement.setDate(1, Date.valueOf(request.getStartDate()));
            callableStatement.setDate(2, Date.valueOf(request.getEndDate()));

            log.info("Executing stored procedure {} with parameters: {}, {}", 
                    STORED_PROCEDURE_NAME, request.getStartDate(), request.getEndDate());

            resultSet = callableStatement.executeQuery();

            return processResultSet(resultSet, request, currentUser);

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

    private long processResultSet(ResultSet resultSet, CbcDataRequestDto request, String currentUser) throws SQLException {
        List<CbcMainRecordEntity> entities = new ArrayList<>();
        long recordCount = 0;

        while (resultSet.next()) {
            try {
                CbcMainRecordEntity mainRecord = mapResultSetToEntity(resultSet, request, currentUser);
                entities.add(mainRecord);
                recordCount++;

                // Batch save - simpler approach without complex transaction management
                if (entities.size() >= BATCH_SIZE) {
                    saveEntitiesBatch(entities, recordCount);
                    entities.clear();
                }
            } catch (Exception e) {
                log.warn("Error processing record {}: {}", recordCount + 1, e.getMessage());
                // Continue processing other records
            }
        }

        // Save remaining records
        if (!entities.isEmpty()) {
            saveEntitiesBatch(entities, recordCount);
        }

        return recordCount;
    }

    @Override
    @Transactional
    public void saveEntitiesBatch(List<CbcMainRecordEntity> entities, long totalProcessed) {
        try {
            mainRecordRepository.saveAll(entities);
            log.info("Saved batch of {} records. Total processed: {}", entities.size(), totalProcessed);
        } catch (Exception e) {
            log.error("Error saving batch: {}", e.getMessage());
            throw new RuntimeException("Failed to save batch of records", e);
        }
    }

    private CbcMainRecordEntity mapResultSetToEntity(ResultSet rs, CbcDataRequestDto request, String currentUser) throws SQLException {
        String batchId = UUID.randomUUID().toString();

        // Create main record
        CbcMainRecordEntity mainRecord = createMainRecord(rs, request, currentUser, batchId);

        // Create and set related entities
        mainRecord.setPersonalInfo(createPersonalInfo(rs, mainRecord, currentUser));
        mainRecord.setIdInformation(createIdInformation(rs, mainRecord, currentUser));
        mainRecord.setAddressInformation(createAddressInformation(rs, mainRecord, currentUser));
        mainRecord.setContactInformation(createContactInformation(rs, mainRecord, currentUser));
        mainRecord.setEmploymentInformation(createEmploymentInformation(rs, mainRecord, currentUser));
        mainRecord.setSecurityInformation(createSecurityInformation(rs, mainRecord, currentUser));
        mainRecord.setLoanInformation(createLoanInformation(rs, mainRecord, currentUser));

        return mainRecord;
    }

    private CbcMainRecordEntity createMainRecord(ResultSet rs, CbcDataRequestDto request, String currentUser, String batchId) throws SQLException {
        CbcMainRecordEntity mainRecord = new CbcMainRecordEntity();
        mainRecord.setBatchId(batchId);
        mainRecord.setRequestStartDate(request.getStartDate());
        mainRecord.setRequestEndDate(request.getEndDate());
        mainRecord.setProcessingDate(LocalDate.now());
        mainRecord.setAccountNumber(rs.getString("Account Number"));
        mainRecord.setCreditorId(rs.getString("CreditorID"));
        mainRecord.setAccountType(rs.getString("AccountType"));
        mainRecord.setAsOfDate(rs.getString("AsofDate"));
        mainRecord.setCreatedBy(currentUser);
        mainRecord.setUpdatedBy(currentUser);
        return mainRecord;
    }

    private CbcPersonalInfoEntity createPersonalInfo(ResultSet rs, CbcMainRecordEntity mainRecord, String currentUser) throws SQLException {
        CbcPersonalInfoEntity personalInfo = new CbcPersonalInfoEntity();
        personalInfo.setMainRecord(mainRecord);
        personalInfo.setDateOfBirth(rs.getString("DateofBirth"));
        personalInfo.setFamilyNameEnglish(rs.getString("FamilyName(English)"));
        personalInfo.setFirstNameEnglish(rs.getString("FirstName(English)"));
        personalInfo.setSecondNameEnglish(rs.getString("SecondName(English)"));
        personalInfo.setThirdNameEnglish(rs.getString("ThirdName(English)"));
        personalInfo.setUnformattedNameEnglish(rs.getString("UnformattedName(English)"));
        personalInfo.setMothersNameUnformattedEnglish(rs.getString("MothersNameUnformatted(English)"));
        personalInfo.setFamilyNameKhmer(rs.getString("FamilyName(Khmer)"));
        personalInfo.setFirstNameKhmer(rs.getString("FirstName(Khmer)"));
        personalInfo.setSecondNameKhmer(rs.getString("SecondName(Khmer)"));
        personalInfo.setThirdNameKhmer(rs.getString("ThirdName(Khmer)"));
        personalInfo.setUnformattedNameKhmer(rs.getString("UnformattedName(Khmer)"));
        personalInfo.setMothersNameUnformattedKhmer(rs.getString("MothersNameUnformatted(Khmer)"));
        personalInfo.setGender(rs.getString("Gender"));
        personalInfo.setMaritalStatus(rs.getString("MaritalStatus"));
        personalInfo.setNationalityCode(rs.getString("NationalityCode"));
        personalInfo.setTaxpayerRegistrationNumber(rs.getString("TaxpayerRegistrationNumber"));
        personalInfo.setApplicantType(rs.getString("ApplicantType"));
        personalInfo.setCreatedBy(currentUser);
        personalInfo.setUpdatedBy(currentUser);
        return personalInfo;
    }

    private CbcIdInformationEntity createIdInformation(ResultSet rs, CbcMainRecordEntity mainRecord, String currentUser) throws SQLException {
        CbcIdInformationEntity idInfo = new CbcIdInformationEntity();
        idInfo.setMainRecord(mainRecord);
        idInfo.setIdType1(rs.getString("IDType-1"));
        idInfo.setIdNumber1(rs.getString("IDNumber-1"));
        idInfo.setIdExpiryDate1(rs.getString("IDExpiryDate-1"));
        idInfo.setIdType2(rs.getString("IDType-2"));
        idInfo.setIdNumber2(rs.getString("IDNumber-2"));
        idInfo.setIdExpiryDate2(rs.getString("IDExpiryDate-2"));
        idInfo.setIdType3(rs.getString("IDType-3"));
        idInfo.setIdNumber3(rs.getString("IDNumber-3"));
        idInfo.setIdExpiryDate3(rs.getString("IDExpiryDate-3"));
        idInfo.setCreatedBy(currentUser);
        idInfo.setUpdatedBy(currentUser);
        return idInfo;
    }

    private CbcAddressInformationEntity createAddressInformation(ResultSet rs, CbcMainRecordEntity mainRecord, String currentUser) throws SQLException {
        CbcAddressInformationEntity addressInfo = new CbcAddressInformationEntity();
        addressInfo.setMainRecord(mainRecord);
        
        // Address 1
        addressInfo.setAddressType1(rs.getString("AddressType-1"));
        addressInfo.setProvince1(rs.getString("Province-1"));
        addressInfo.setDistrict1(rs.getString("District-1"));
        addressInfo.setCommune1(rs.getString("Commune-1"));
        addressInfo.setVillage1(rs.getString("Village-1"));
        addressInfo.setAddress1Field1English(rs.getString("Address-1Field1(English)"));
        addressInfo.setAddress1Field2English(rs.getString("Address-1Field2(English)"));
        addressInfo.setAddress1Field1Khmer(rs.getString("Address-1Field1(Khmer)"));
        addressInfo.setAddress1Field2Khmer(rs.getString("Address-1Field2(Khmer)"));
        addressInfo.setCity1English(rs.getString("City-1(English)"));
        addressInfo.setCity1Khmer(rs.getString("City-1(Khmer)"));
        addressInfo.setCountry1(rs.getString("Country-1"));
        addressInfo.setPostalCode1(rs.getString("PostalCode-1"));
        
        // Address 2
        addressInfo.setAddressType2(rs.getString("AddressType-2"));
        addressInfo.setProvince2(rs.getString("Province-2"));
        addressInfo.setDistrict2(rs.getString("District-2"));
        addressInfo.setCommune2(rs.getString("Commune-2"));
        addressInfo.setVillage2(rs.getString("Village-2"));
        addressInfo.setAddress2Field1English(rs.getString("Address-2Field1(English)"));
        addressInfo.setAddress2Field2English(rs.getString("Address-2Field2(English)"));
        addressInfo.setAddress2Field1Khmer(rs.getString("Address-2Field1(Khmer)"));
        addressInfo.setAddress2Field2Khmer(rs.getString("Address-2Field2(Khmer)"));
        addressInfo.setCity2English(rs.getString("City-2(English)"));
        addressInfo.setCity2Khmer(rs.getString("City-2(Khmer)"));
        addressInfo.setCountry2(rs.getString("Country-2"));
        addressInfo.setPostalCode2(rs.getString("PostalCode-2"));
        
        // Address 3
        addressInfo.setAddress3Type(rs.getString("Address-3Type"));
        addressInfo.setProvince3(rs.getString("Province-3"));
        addressInfo.setDistrict3(rs.getString("District-3"));
        addressInfo.setCommune3(rs.getString("Commune-3"));
        addressInfo.setVillage3(rs.getString("Village-3"));
        addressInfo.setAddress3Field1English(rs.getString("Address-3Field1(English)"));
        addressInfo.setAddress3Field2English(rs.getString("Address-3Field2(English)"));
        addressInfo.setAddress3Field1Khmer(rs.getString("Address-3Field1(Khmer)"));
        addressInfo.setAddress3Field2Khmer(rs.getString("Address-3Field2(Khmer)"));
        addressInfo.setCity3English(rs.getString("City-3(English)"));
        addressInfo.setCity3Khmer(rs.getString("City-3(Khmer)"));
        addressInfo.setCountry3(rs.getString("Country-3"));
        addressInfo.setPostalCode3(rs.getString("PostalCode-3"));
        
        addressInfo.setCreatedBy(currentUser);
        addressInfo.setUpdatedBy(currentUser);
        return addressInfo;
    }

    private CbcContactInformationEntity createContactInformation(ResultSet rs, CbcMainRecordEntity mainRecord, String currentUser) throws SQLException {
        CbcContactInformationEntity contactInfo = new CbcContactInformationEntity();
        contactInfo.setMainRecord(mainRecord);
        contactInfo.setEmailAddress(rs.getString("EmailAddress"));
        
        // Contact 1
        contactInfo.setContactNumberType1(rs.getString("ContactNumberType1"));
        contactInfo.setContactNumberCountryCode1(rs.getString("ContactNumber–CountryCode1"));
        contactInfo.setContactNumberArea1(rs.getString("ContactNumber–Area1"));
        contactInfo.setContactNumberNumber1(rs.getString("ContactNumber–Number1"));
        contactInfo.setContactNumberExtension1(rs.getString("ContactNumber–Extension1"));
        
        // Contact 2
        contactInfo.setContactNumberType2(rs.getString("ContactNumberType2"));
        contactInfo.setContactNumberCountryCode2(rs.getString("ContactNumber–CountryCode2"));
        contactInfo.setContactNumberArea2(rs.getString("ContactNumber–Area2"));
        contactInfo.setContactNumberNumber2(rs.getString("ContactNumber–Number2"));
        contactInfo.setContactNumberExtension2(rs.getString("ContactNumber–Extension2"));
        
        // Contact 3
        contactInfo.setContactNumberType3(rs.getString("ContactNumberType3"));
        contactInfo.setContactNumberCountryCode3(rs.getString("ContactNumber–CountryCode3"));
        contactInfo.setContactNumberArea3(rs.getString("ContactNumber–Area3"));
        contactInfo.setContactNumberNumber3(rs.getString("ContactNumber–Number3"));
        contactInfo.setContactNumberExtension3(rs.getString("ContactNumber–Extension3"));
        
        contactInfo.setCreatedBy(currentUser);
        contactInfo.setUpdatedBy(currentUser);
        return contactInfo;
    }

    private CbcEmploymentInformationEntity createEmploymentInformation(ResultSet rs, CbcMainRecordEntity mainRecord, String currentUser) throws SQLException {
        CbcEmploymentInformationEntity employmentInfo = new CbcEmploymentInformationEntity();
        employmentInfo.setMainRecord(mainRecord);
        
        // Employment 1
        employmentInfo.setEmploymentStatus1(rs.getString("Employment Status-1"));
        employmentInfo.setEmploymentType1(rs.getString("Employment Type-1"));
        employmentInfo.setEmployer1NameEnglish(rs.getString("Employer-1Name(English)"));
        employmentInfo.setEmployer1NameKhmer(rs.getString("Employer-1Name(Khmer)"));
        employmentInfo.setEconomicSector1(rs.getString("EconomicSector-1"));
        employmentInfo.setBusinessType1(rs.getString("BusinessType-1"));
        employmentInfo.setEmployer1AddressEnglish(rs.getString("Employer-1'sAddress(English)"));
        employmentInfo.setEmployer1AddressKhmer(rs.getString("Employer-1'sAddress(Khmer)"));
        employmentInfo.setEmployer1Province(rs.getString("Employer-1'sProvince"));
        employmentInfo.setEmployer1District(rs.getString("Employer-1'sDistrict"));
        employmentInfo.setEmployer1Commune(rs.getString("Employer-1'sCommune"));
        employmentInfo.setEmployer1Village(rs.getString("Employer-1'sVillage"));
        employmentInfo.setEmployer1AddressCityEnglish(rs.getString("Employer-1'sAddressCity(English)"));
        employmentInfo.setEmployer1AddressCityKhmer(rs.getString("Employer-1'sAddressCity(Khmer)"));
        employmentInfo.setEmp1Country1(rs.getString("EMP1Country-1"));
        employmentInfo.setEmp1PostalCode1(rs.getString("EMP1PostalCode-1"));
        employmentInfo.setOccupation1English(rs.getString("Occupation-1(English)"));
        employmentInfo.setOccupation1Khmer(rs.getString("Occupation-1(Khmer)"));
        employmentInfo.setDateOfEmployment1(rs.getString("DateofEmployment-1"));
        employmentInfo.setLengthOfService1Months(rs.getString("LengthofService-1(Months)"));
        employmentInfo.setContractExpiryDate1(rs.getString("ContractExpiryDate-1"));
        employmentInfo.setCurrency1(rs.getString("Currency-1"));
        employmentInfo.setMonthlyBasicSalaryIncome1(getBigDecimalFromResultSet(rs, "MonthlyBasicSalary/Income-1"));
        employmentInfo.setTotalMonthlySalaryIncome1(getBigDecimalFromResultSet(rs, "TotalMonthlySalary/Income-1"));
        
        // Employment 2
        employmentInfo.setEmployerType2(rs.getString("Employer-2Type"));
        employmentInfo.setSelfEmployed2(rs.getString("SelfEmployed-2"));
        employmentInfo.setEmployer2NameEnglish(rs.getString("Employer-2Name(English)"));
        employmentInfo.setEmployer2NameKhmer(rs.getString("Employer-2Name(Khmer)"));
        employmentInfo.setEconomicSector2(rs.getString("EconomicSector-2"));
        employmentInfo.setBusinessType2(rs.getString("BusinessType-2"));
        employmentInfo.setEmployer2AddressEnglish(rs.getString("Employer-2'sAddress(English)"));
        employmentInfo.setEmployer2AddressKhmer(rs.getString("Employer-2'sAddress(Khmer)"));
        employmentInfo.setEmployer2Province(rs.getString("Employer-2'sProvince"));
        employmentInfo.setEmployer2District(rs.getString("Employer-2'sDistrict"));
        employmentInfo.setEmployer2Commune(rs.getString("Employer-2'sCommune"));
        employmentInfo.setEmployer2Village(rs.getString("Employer-2'sVillage"));
        employmentInfo.setEmployer2AddressCityEnglish(rs.getString("Employer-2'sAddressCity(English)"));
        employmentInfo.setEmployer2AddressCityKhmer(rs.getString("Employer-2'sAddressCity(Khmer)"));
        employmentInfo.setEmzCountry2(rs.getString("EMZCountry-2"));
        employmentInfo.setEmzPostalCode2(rs.getString("EMZPostalCode-2"));
        employmentInfo.setOccupation2English(rs.getString("Occupation-2(English)"));
        employmentInfo.setOccupation2Khmer(rs.getString("Occupation-2(Khmer)"));
        employmentInfo.setDateOfEmployment2(rs.getString("DateofEmployment-2"));
        employmentInfo.setLengthOfService2Months(rs.getString("LengthofService-2(Months)"));
        employmentInfo.setContractExpiryDate2(rs.getString("ContractExpiryDate-2"));
        employmentInfo.setCurrency2(rs.getString("Currency–2"));
        employmentInfo.setMonthlyBasicSalaryIncome2(getBigDecimalFromResultSet(rs, "MonthlyBasicSalary/Income-2"));
        employmentInfo.setTotalMonthlySalaryIncome2(getBigDecimalFromResultSet(rs, "TotalMonthlySalary/Income-2"));
        
        // Employment 3
        employmentInfo.setEmployerType3(rs.getString("EmployerType-3"));
        employmentInfo.setSelfEmployed3(rs.getString("SelfEmployed-3"));
        employmentInfo.setEmployer3NameEnglish(rs.getString("Employer-3Name(English)"));
        
        employmentInfo.setCreatedBy(currentUser);
        employmentInfo.setUpdatedBy(currentUser);
        return employmentInfo;
    }

    private CbcSecurityInformationEntity createSecurityInformation(ResultSet rs, CbcMainRecordEntity mainRecord, String currentUser) throws SQLException {
        CbcSecurityInformationEntity securityInfo = new CbcSecurityInformationEntity();
        securityInfo.setMainRecord(mainRecord);
        
        // Security 1
        securityInfo.setSecurityType1(rs.getString("Security Type-1"));
        securityInfo.setSecurityNumber1(rs.getString("Security Number-1"));
        securityInfo.setSecurityCurrency1(rs.getString("Security Currency-1"));
        securityInfo.setSecurityValue1(getBigDecimalFromResultSet(rs, "Security Value-1"));
        securityInfo.setSecurityLocation1(rs.getString("Security Location-1"));
        
        // Security 2
        securityInfo.setSecurityType2(rs.getString("Security Type-2"));
        securityInfo.setSecurityNumber2(rs.getString("Security Number-2"));
        securityInfo.setSecurityCurrency2(rs.getString("Security Currency-2"));
        securityInfo.setSecurityValue2(getBigDecimalFromResultSet(rs, "Security Value-2"));
        securityInfo.setSecurityLocation2(rs.getString("Security Location-2"));
        
        // Security 3
        securityInfo.setSecurityType3(rs.getString("Security Type-3"));
        securityInfo.setSecurityNumber3(rs.getString("Security Number-3"));
        securityInfo.setSecurityCurrency3(rs.getString("Security Currency-3"));
        securityInfo.setSecurityValue3(getBigDecimalFromResultSet(rs, "Security Value-3"));
        securityInfo.setSecurityLocation3(rs.getString("Security Location-3"));
        
        securityInfo.setSecurityTypePrimary(rs.getString("SecurityType-Primary"));
        securityInfo.setSpecialNote(rs.getString("Special Note"));
        securityInfo.setEnquiryMemberReference(rs.getString("Enquiry Member Reference"));
        securityInfo.setLoanToSectorSection(rs.getString("Loan to sector/Section"));
        securityInfo.setCurrency3(rs.getString("Currency-3"));
        securityInfo.setBranchAddressCode(rs.getString("Branch Address Code"));
        
        securityInfo.setCreatedBy(currentUser);
        securityInfo.setUpdatedBy(currentUser);
        return securityInfo;
    }

    private CbcLoanInformationEntity createLoanInformation(ResultSet rs, CbcMainRecordEntity mainRecord, String currentUser) throws SQLException {
        CbcLoanInformationEntity loanInfo = new CbcLoanInformationEntity();
        loanInfo.setMainRecord(mainRecord);
        loanInfo.setLoanTermType(rs.getString("Loan Term Type"));
        loanInfo.setGroupAccountReference(rs.getString("Group Account Reference"));
        loanInfo.setDateIssued(rs.getString("Date Issued"));
        loanInfo.setProductType(rs.getString("ProductType"));
        loanInfo.setCurrency(rs.getString("Currency"));
        loanInfo.setProductLimitOriginalAmount(getBigDecimalFromResultSet(rs, "ProductLimit/OriginalAmount"));
        loanInfo.setProductExpiryDate(rs.getString("ProductExpiryDate"));
        loanInfo.setProductStatus(rs.getString("ProductStatus"));
        loanInfo.setRestructuredLoan(rs.getString("Restructured Loan"));
        loanInfo.setInstalmentAmount(getBigDecimalFromResultSet(rs, "InstalmentAmount"));
        loanInfo.setPaymentFrequency(rs.getString("PaymentFrequency"));
        loanInfo.setTenure(rs.getString("Tenure"));
        loanInfo.setLastPaymentDate(rs.getString("LastPaymentDate"));
        loanInfo.setLastAmountPaid(getBigDecimalFromResultSet(rs, "LastAmountPaid"));
        loanInfo.setOutstandingBalance(getBigDecimalFromResultSet(rs, "OutstandingBalance"));
        loanInfo.setPastDue(getBigDecimalFromResultSet(rs, "PastDue"));
        loanInfo.setNextPaymentDate(rs.getString("NextPaymentDate"));
        loanInfo.setPaymentStatusCode(rs.getString("PaymentStatusCode"));
        loanInfo.setLossStatus(rs.getString("LossStatus"));
        loanInfo.setLossStatusDate(rs.getString("LossStatusDate"));
        loanInfo.setOriginalAmountAsAtLoadDate(rs.getString("OriginalAmountasatLoadDate"));
        loanInfo.setEmzOutstandingBalance(rs.getString("EMZOutstandingBalance"));
        loanInfo.setCreatedBy(currentUser);
        loanInfo.setUpdatedBy(currentUser);
        return loanInfo;
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

    @Override
    public DataLoadStatusDto getLoadStatus() {
        return currentLoadStatus;
    }

    @Override
    public List<CbcMainRecordResponseDto> getAllCbcRecords() {
        log.info("Fetching all CBC records");
        List<CbcMainRecordEntity> entities = mainRecordRepository.findAll();
        return entities.stream()
                .map(cbcMapper::toMainRecordResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaginationResponse<CbcMainRecordResponseDto> getCbcRecordsPaginated(CbcFilterRequestDto filterRequest) {
        log.info("Fetching CBC records with pagination. Page: {}, Size: {}", 
                filterRequest.getPage(), filterRequest.getSize());

        // Create specification for filtering
        Specification<CbcMainRecordEntity> spec = CbcMainRecordSpecification.withFilters(
                filterRequest.getStartDate(),
                filterRequest.getEndDate(),
                filterRequest.getSearch()
        );

        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                          filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage() - 1, filterRequest.getSize(), sort);

        // Fetch data
        Page<CbcMainRecordEntity> page = mainRecordRepository.findAll(spec, pageable);

        // Convert to response using mapper
        List<CbcMainRecordResponseDto> content = page.getContent().stream()
                .map(cbcMapper::toMainRecordResponseDto)
                .collect(Collectors.toList());

        return createPaginationResponse(content, page, filterRequest);
    }

    private PaginationResponse<CbcMainRecordResponseDto> createPaginationResponse(
            List<CbcMainRecordResponseDto> content, 
            Page<CbcMainRecordEntity> page, 
            CbcFilterRequestDto filterRequest) {
        
        PaginationResponse<CbcMainRecordResponseDto> response = new PaginationResponse<>();
        response.setContent(content);
        response.setPageNo(filterRequest.getPage());
        response.setPageSize(filterRequest.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setTotalCount((int) page.getTotalElements());
        return response;
    }

    @Override
    public CbcMainRecordResponseDto getCbcRecordById(Long id) {
        log.info("Fetching CBC record by ID: {}", id);
        CbcMainRecordEntity entity = mainRecordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CBC record not found with ID: " + id));
        return cbcMapper.toMainRecordResponseDto(entity);
    }

    @Override
    @Transactional
    public CbcMainRecordResponseDto updateCbcRecord(Long id, CbcUpdateRequestDto updateRequest) {
        log.info("Updating CBC record with ID: {}", id);
        
        String currentUser = getCurrentUsername();
        
        CbcMainRecordEntity entity = mainRecordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CBC record not found with ID: " + id));

        // Update using mapper
        updateEntityWithMapper(entity, updateRequest, currentUser);

        CbcMainRecordEntity savedEntity = mainRecordRepository.save(entity);
        log.info("CBC record updated successfully with ID: {}", id);
        
        return cbcMapper.toMainRecordResponseDto(savedEntity);
    }

    private void updateEntityWithMapper(CbcMainRecordEntity entity, CbcUpdateRequestDto updateRequest, String currentUser) {
        // Update main record
        cbcMapper.updateMainRecordFromDto(updateRequest, entity);
        entity.setUpdatedBy(currentUser);

        // Update related entities using mapper
        updateRelatedEntities(entity, updateRequest, currentUser);
    }

    private void updateRelatedEntities(CbcMainRecordEntity entity, CbcUpdateRequestDto updateRequest, String currentUser) {
        if (entity.getPersonalInfo() != null) {
            cbcMapper.updatePersonalInfoFromDto(updateRequest, entity.getPersonalInfo());
            entity.getPersonalInfo().setUpdatedBy(currentUser);
        }

        if (entity.getIdInformation() != null) {
            cbcMapper.updateIdInformationFromDto(updateRequest, entity.getIdInformation());
            entity.getIdInformation().setUpdatedBy(currentUser);
        }

        if (entity.getAddressInformation() != null) {
            cbcMapper.updateAddressInformationFromDto(updateRequest, entity.getAddressInformation());
            entity.getAddressInformation().setUpdatedBy(currentUser);
        }

        if (entity.getContactInformation() != null) {
            cbcMapper.updateContactInformationFromDto(updateRequest, entity.getContactInformation());
            entity.getContactInformation().setUpdatedBy(currentUser);
        }

        if (entity.getEmploymentInformation() != null) {
            cbcMapper.updateEmploymentInformationFromDto(updateRequest, entity.getEmploymentInformation());
            entity.getEmploymentInformation().setUpdatedBy(currentUser);
        }

        if (entity.getSecurityInformation() != null) {
            cbcMapper.updateSecurityInformationFromDto(updateRequest, entity.getSecurityInformation());
            entity.getSecurityInformation().setUpdatedBy(currentUser);
        }

        if (entity.getLoanInformation() != null) {
            cbcMapper.updateLoanInformationFromDto(updateRequest, entity.getLoanInformation());
            entity.getLoanInformation().setUpdatedBy(currentUser);
        }
    }

    @Override
    @Transactional
    public void deleteCbcRecord(Long id) {
        log.info("Deleting CBC record with ID: {}", id);
        
        if (!mainRecordRepository.existsById(id)) {
            throw new NotFoundException("CBC record not found with ID: " + id);
        }
        
        mainRecordRepository.deleteById(id);
        log.info("CBC record deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteAllCbcRecords() {
        log.info("Deleting all CBC records");
        mainRecordRepository.deleteAllData();
        
        // Reset status
        currentLoadStatus = new DataLoadStatusDto(
                "IDLE", "All records deleted. No data loading operation in progress", 
                0L, null, null, null);
        
        log.info("All CBC records deleted successfully");
    }

    @Override
    public long getTotalRecordsCount() {
        return mainRecordRepository.countAllRecords();
    }
}