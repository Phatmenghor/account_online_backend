package com.internal.feature.report_staging.mapper;

import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Slf4j
public class CbcResultSetMapper {

    public CbcStagingRecordEntity mapResultSetToStagingEntity(ResultSet rs, String currentUser) throws SQLException {
        CbcStagingRecordEntity entity = new CbcStagingRecordEntity();
        
        try {
            // Main Record Fields
            entity.setAccountNumber(getStringValue(rs, "Account Number"));
            entity.setCreditorId(getStringValue(rs, "CreditorID"));
            entity.setAccountType(getStringValue(rs, "AccountType"));
            entity.setAsOfDate(getStringValue(rs, "AsofDate"));
            
            // Personal Info Fields
            entity.setDateOfBirth(getStringValue(rs, "DateofBirth"));
            entity.setFamilyNameEnglish(getStringValue(rs, "FamilyName(English)"));
            entity.setFirstNameEnglish(getStringValue(rs, "FirstName(English)"));
            entity.setSecondNameEnglish(getStringValue(rs, "SecondName(English)"));
            entity.setThirdNameEnglish(getStringValue(rs, "ThirdName(English)"));
            entity.setUnformattedNameEnglish(getStringValue(rs, "UnformattedName(English)"));
            entity.setMothersNameUnformattedEnglish(getStringValue(rs, "MothersNameUnformatted(English)"));
            entity.setFamilyNameKhmer(getStringValue(rs, "FamilyName(Khmer)"));
            entity.setFirstNameKhmer(getStringValue(rs, "FirstName(Khmer)"));
            entity.setSecondNameKhmer(getStringValue(rs, "SecondName(Khmer)"));
            entity.setThirdNameKhmer(getStringValue(rs, "ThirdName(Khmer)"));
            entity.setUnformattedNameKhmer(getStringValue(rs, "UnformattedName(Khmer)"));
            entity.setMothersNameUnformattedKhmer(getStringValue(rs, "MothersNameUnformatted(Khmer)"));
            entity.setGender(getStringValue(rs, "Gender"));
            entity.setMaritalStatus(getStringValue(rs, "MaritalStatus"));
            entity.setNationalityCode(getStringValue(rs, "NationalityCode"));
            entity.setTaxpayerRegistrationNumber(getStringValue(rs, "TaxpayerRegistrationNumber"));
            entity.setApplicantType(getStringValue(rs, "ApplicantType"));
            
            // ID Information Fields
            entity.setIdType1(getStringValue(rs, "IDType-1"));
            entity.setIdNumber1(getStringValue(rs, "IDNumber-1"));
            entity.setIdExpiryDate1(getStringValue(rs, "IDExpiryDate-1"));
            entity.setIdType2(getStringValue(rs, "IDType-2"));
            entity.setIdNumber2(getStringValue(rs, "IDNumber-2"));
            entity.setIdExpiryDate2(getStringValue(rs, "IDExpiryDate-2"));
            entity.setIdType3(getStringValue(rs, "IDType-3"));
            entity.setIdNumber3(getStringValue(rs, "IDNumber-3"));
            entity.setIdExpiryDate3(getStringValue(rs, "IDExpiryDate-3"));
            
            // Address Information Fields - Address 1
            entity.setAddressType1(getStringValue(rs, "AddressType-1"));
            entity.setProvince1(getStringValue(rs, "Province-1"));
            entity.setDistrict1(getStringValue(rs, "District-1"));
            entity.setCommune1(getStringValue(rs, "Commune-1"));
            entity.setVillage1(getStringValue(rs, "Village-1"));
            entity.setAddress1Field1English(getStringValue(rs, "Address-1Field1(English)"));
            entity.setAddress1Field2English(getStringValue(rs, "Address-1Field2(English)"));
            entity.setAddress1Field1Khmer(getStringValue(rs, "Address-1Field1(Khmer)"));
            entity.setAddress1Field2Khmer(getStringValue(rs, "Address-1Field2(Khmer)"));
            entity.setCity1English(getStringValue(rs, "City-1(English)"));
            entity.setCity1Khmer(getStringValue(rs, "City-1(Khmer)"));
            entity.setCountry1(getStringValue(rs, "Country-1"));
            entity.setPostalCode1(getStringValue(rs, "PostalCode-1"));
            
            // Address 2
            entity.setAddressType2(getStringValue(rs, "AddressType-2"));
            entity.setProvince2(getStringValue(rs, "Province-2"));
            entity.setDistrict2(getStringValue(rs, "District-2"));
            entity.setCommune2(getStringValue(rs, "Commune-2"));
            entity.setVillage2(getStringValue(rs, "Village-2"));
            entity.setAddress2Field1English(getStringValue(rs, "Address-2Field1(English)"));
            entity.setAddress2Field2English(getStringValue(rs, "Address-2Field2(English)"));
            entity.setAddress2Field1Khmer(getStringValue(rs, "Address-2Field1(Khmer)"));
            entity.setAddress2Field2Khmer(getStringValue(rs, "Address-2Field2(Khmer)"));
            entity.setCity2English(getStringValue(rs, "City-2(English)"));
            entity.setCity2Khmer(getStringValue(rs, "City-2(Khmer)"));
            entity.setCountry2(getStringValue(rs, "Country-2"));
            entity.setPostalCode2(getStringValue(rs, "PostalCode-2"));
            
            // Address 3
            entity.setAddress3Type(getStringValue(rs, "Address-3Type"));
            entity.setProvince3(getStringValue(rs, "Province-3"));
            entity.setDistrict3(getStringValue(rs, "District-3"));
            entity.setCommune3(getStringValue(rs, "Commune-3"));
            entity.setVillage3(getStringValue(rs, "Village-3"));
            entity.setAddress3Field1English(getStringValue(rs, "Address-3Field1(English)"));
            entity.setAddress3Field2English(getStringValue(rs, "Address-3Field2(English)"));
            entity.setAddress3Field1Khmer(getStringValue(rs, "Address-3Field1(Khmer)"));
            entity.setAddress3Field2Khmer(getStringValue(rs, "Address-3Field2(Khmer)"));
            entity.setCity3English(getStringValue(rs, "City-3(English)"));
            entity.setCity3Khmer(getStringValue(rs, "City-3(Khmer)"));
            entity.setCountry3(getStringValue(rs, "Country-3"));
            entity.setPostalCode3(getStringValue(rs, "PostalCode-3"));
            
            // Contact Information Fields
            entity.setEmailAddress(getStringValue(rs, "EmailAddress"));
            entity.setContactNumberType1(getStringValue(rs, "ContactNumberType1"));
            entity.setContactNumberCountryCode1(getStringValue(rs, "ContactNumber–CountryCode1"));
            entity.setContactNumberArea1(getStringValue(rs, "ContactNumber–Area1"));
            entity.setContactNumberNumber1(getStringValue(rs, "ContactNumber–Number1"));
            entity.setContactNumberExtension1(getStringValue(rs, "ContactNumber–Extension1"));
            entity.setContactNumberType2(getStringValue(rs, "ContactNumberType2"));
            entity.setContactNumberCountryCode2(getStringValue(rs, "ContactNumber–CountryCode2"));
            entity.setContactNumberArea2(getStringValue(rs, "ContactNumber–Area2"));
            entity.setContactNumberNumber2(getStringValue(rs, "ContactNumber–Number2"));
            entity.setContactNumberExtension2(getStringValue(rs, "ContactNumber–Extension2"));
            entity.setContactNumberType3(getStringValue(rs, "ContactNumberType3"));
            entity.setContactNumberCountryCode3(getStringValue(rs, "ContactNumber–CountryCode3"));
            entity.setContactNumberArea3(getStringValue(rs, "ContactNumber–Area3"));
            entity.setContactNumberNumber3(getStringValue(rs, "ContactNumber–Number3"));
            entity.setContactNumberExtension3(getStringValue(rs, "ContactNumber–Extension3"));
            
            // Employment Information Fields - Employment 1
            entity.setEmploymentStatus1(getStringValue(rs, "Employment Status-1"));
            entity.setEmploymentType1(getStringValue(rs, "Employment Type-1"));
            entity.setEmployer1NameEnglish(getStringValue(rs, "Employer-1Name(English)"));
            entity.setEmployer1NameKhmer(getStringValue(rs, "Employer-1Name(Khmer)"));
            entity.setEconomicSector1(getStringValue(rs, "EconomicSector-1"));
            entity.setBusinessType1(getStringValue(rs, "BusinessType-1"));
            entity.setEmployer1AddressEnglish(getStringValue(rs, "Employer-1'sAddress(English)"));
            entity.setEmployer1AddressKhmer(getStringValue(rs, "Employer-1'sAddress(Khmer)"));
            entity.setEmployer1Province(getStringValue(rs, "Employer-1'sProvince"));
            entity.setEmployer1District(getStringValue(rs, "Employer-1'sDistrict"));
            entity.setEmployer1Commune(getStringValue(rs, "Employer-1'sCommune"));
            entity.setEmployer1Village(getStringValue(rs, "Employer-1'sVillage"));
            entity.setEmployer1AddressCityEnglish(getStringValue(rs, "Employer-1'sAddressCity(English)"));
            entity.setEmployer1AddressCityKhmer(getStringValue(rs, "Employer-1'sAddressCity(Khmer)"));
            entity.setEmp1Country1(getStringValue(rs, "EMP1Country-1"));
            entity.setEmp1PostalCode1(getStringValue(rs, "EMP1PostalCode-1"));
            entity.setOccupation1English(getStringValue(rs, "Occupation-1(English)"));
            entity.setOccupation1Khmer(getStringValue(rs, "Occupation-1(Khmer)"));
            entity.setDateOfEmployment1(getStringValue(rs, "DateofEmployment-1"));
            entity.setLengthOfService1Months(getStringValue(rs, "LengthofService-1(Months)"));
            entity.setContractExpiryDate1(getStringValue(rs, "ContractExpiryDate-1"));
            entity.setCurrency1(getStringValue(rs, "Currency-1"));
            entity.setMonthlyBasicSalaryIncome1(getBigDecimalValue(rs, "MonthlyBasicSalary/Income-1"));
            entity.setTotalMonthlySalaryIncome1(getBigDecimalValue(rs, "TotalMonthlySalary/Income-1"));
            
            // Employment 2
            entity.setEmployerType2(getStringValue(rs, "Employer-2Type"));
            entity.setSelfEmployed2(getStringValue(rs, "SelfEmployed-2"));
            entity.setEmployer2NameEnglish(getStringValue(rs, "Employer-2Name(English)"));
            entity.setEmployer2NameKhmer(getStringValue(rs, "Employer-2Name(Khmer)"));
            entity.setEconomicSector2(getStringValue(rs, "EconomicSector-2"));
            entity.setBusinessType2(getStringValue(rs, "BusinessType-2"));
            entity.setEmployer2AddressEnglish(getStringValue(rs, "Employer-2'sAddress(English)"));
            entity.setEmployer2AddressKhmer(getStringValue(rs, "Employer-2'sAddress(Khmer)"));
            entity.setEmployer2Province(getStringValue(rs, "Employer-2'sProvince"));
            entity.setEmployer2District(getStringValue(rs, "Employer-2'sDistrict"));
            entity.setEmployer2Commune(getStringValue(rs, "Employer-2'sCommune"));
            entity.setEmployer2Village(getStringValue(rs, "Employer-2'sVillage"));
            entity.setEmployer2AddressCityEnglish(getStringValue(rs, "Employer-2'sAddressCity(English)"));
            entity.setEmployer2AddressCityKhmer(getStringValue(rs, "Employer-2'sAddressCity(Khmer)"));
            entity.setEmzCountry2(getStringValue(rs, "EMZCountry-2"));
            entity.setEmzPostalCode2(getStringValue(rs, "EMZPostalCode-2"));
            entity.setOccupation2English(getStringValue(rs, "Occupation-2(English)"));
            entity.setOccupation2Khmer(getStringValue(rs, "Occupation-2(Khmer)"));
            entity.setDateOfEmployment2(getStringValue(rs, "DateofEmployment-2"));
            entity.setLengthOfService2Months(getStringValue(rs, "LengthofService-2(Months)"));
            entity.setContractExpiryDate2(getStringValue(rs, "ContractExpiryDate-2"));
            entity.setCurrency2(getStringValue(rs, "Currency–2"));
            entity.setMonthlyBasicSalaryIncome2(getBigDecimalValue(rs, "MonthlyBasicSalary/Income-2"));
            entity.setTotalMonthlySalaryIncome2(getBigDecimalValue(rs, "TotalMonthlySalary/Income-2"));
            
            // Employment 3
            entity.setEmployerType3(getStringValue(rs, "EmployerType-3"));
            entity.setSelfEmployed3(getStringValue(rs, "SelfEmployed-3"));
            entity.setEmployer3NameEnglish(getStringValue(rs, "Employer-3Name(English)"));
            entity.setEmployer3NameKhmer(getStringValue(rs, "Employer-3Name(Khmer)"));
            entity.setEconomicSector3(getStringValue(rs, "EconomicSector-3"));
            entity.setBusinessType3(getStringValue(rs, "BusinessType-3"));
            entity.setEmployer3AddressEnglish(getStringValue(rs, "Employer-3'sAddress(English)"));
            entity.setEmployer3AddressKhmer(getStringValue(rs, "Employer-3'sAddress(Khmer)"));
            entity.setEmployer3Province(getStringValue(rs, "Employer-3'sProvince"));
            entity.setEmployer3District(getStringValue(rs, "Employer-3'sDistrict"));
            entity.setEmployer3Commune(getStringValue(rs, "Employer-3'sCommune"));
            entity.setEmployer3Village(getStringValue(rs, "Employer-3'sVillage"));
            entity.setEmployer3AddressCityEnglish(getStringValue(rs, "Employer-3'sAddressCity(English)"));
            entity.setEmployer3AddressCityKhmer(getStringValue(rs, "Employer-3'sAddressCity(Khmer)"));
            entity.setEmp3Country3(getStringValue(rs, "EMP3Country-3"));
            entity.setEmp3PostalCode3(getStringValue(rs, "EMP3PostalCode-3"));
            entity.setOccupation3English(getStringValue(rs, "Occupation-3(English)"));
            entity.setOccupation3Khmer(getStringValue(rs, "Occupation-3(Khmer)"));
            entity.setDateOfEmployment3(getStringValue(rs, "DateofEmployment-3"));
            entity.setLengthOfService3Months(getStringValue(rs, "LengthofService-3(Months)"));
            entity.setContractExpiryDate3(getStringValue(rs, "ContractExpiryDate-3"));
            entity.setCurrency3(getStringValue(rs, "Currency-3"));
            entity.setMonthlyBasicSalaryIncome3(getBigDecimalValue(rs, "MonthlyBasicSalary/Income-3"));
            entity.setTotalMonthlySalaryIncome3(getBigDecimalValue(rs, "TotalMonthlySalary/Income-3"));
            
            // Security Information Fields
            entity.setSecurityType1(getStringValue(rs, "Security Type-1"));
            entity.setSecurityNumber1(getStringValue(rs, "Security Number-1"));
            entity.setSecurityCurrency1(getStringValue(rs, "Security Currency-1"));
            entity.setSecurityValue1(getBigDecimalValue(rs, "Security Value-1"));
            entity.setSecurityLocation1(getStringValue(rs, "Security Location-1"));
            entity.setSecurityType2(getStringValue(rs, "Security Type-2"));
            entity.setSecurityNumber2(getStringValue(rs, "Security Number-2"));
            entity.setSecurityCurrency2(getStringValue(rs, "Security Currency-2"));
            entity.setSecurityValue2(getBigDecimalValue(rs, "Security Value-2"));
            entity.setSecurityLocation2(getStringValue(rs, "Security Location-2"));
            entity.setSecurityType3(getStringValue(rs, "Security Type-3"));
            entity.setSecurityNumber3(getStringValue(rs, "Security Number-3"));
            entity.setSecurityCurrency3(getStringValue(rs, "Security Currency-3"));
            entity.setSecurityValue3(getBigDecimalValue(rs, "Security Value-3"));
            entity.setSecurityLocation3(getStringValue(rs, "Security Location-3"));
            entity.setSecurityTypePrimary(getStringValue(rs, "SecurityType-Primary"));
            entity.setSpecialNote(getStringValue(rs, "Special Note"));
            entity.setEnquiryMemberReference(getStringValue(rs, "Enquiry Member Reference"));
            entity.setLoanToSectorSection(getStringValue(rs, "Loan to sector/Section"));
            entity.setBranchAddressCode(getStringValue(rs, "Branch Address Code"));
            
            // Loan Information Fields
            entity.setLoanTermType(getStringValue(rs, "Loan Term Type"));
            entity.setGroupAccountReference(getStringValue(rs, "Group Account Reference"));
            entity.setDateIssued(getStringValue(rs, "Date Issued"));
            entity.setProductType(getStringValue(rs, "ProductType"));
            entity.setCurrency(getStringValue(rs, "Currency"));
            entity.setProductLimitOriginalAmount(getBigDecimalValue(rs, "ProductLimit/OriginalAmount"));
            entity.setProductExpiryDate(getStringValue(rs, "ProductExpiryDate"));
            entity.setProductStatus(getStringValue(rs, "ProductStatus"));
            entity.setRestructuredLoan(getStringValue(rs, "Restructured Loan"));
            entity.setInstalmentAmount(getBigDecimalValue(rs, "InstalmentAmount"));
            entity.setPaymentFrequency(getStringValue(rs, "PaymentFrequency"));
            entity.setTenure(getStringValue(rs, "Tenure"));
            entity.setLastPaymentDate(getStringValue(rs, "LastPaymentDate"));
            entity.setLastAmountPaid(getBigDecimalValue(rs, "LastAmountPaid"));
            entity.setOutstandingBalance(getBigDecimalValue(rs, "OutstandingBalance"));
            entity.setPastDue(getBigDecimalValue(rs, "PastDue"));
            entity.setNextPaymentDate(getStringValue(rs, "NextPaymentDate"));
            entity.setPaymentStatusCode(getStringValue(rs, "PaymentStatusCode"));
            entity.setLossStatus(getStringValue(rs, "LossStatus"));
            entity.setLossStatusDate(getStringValue(rs, "LossStatusDate"));
            entity.setOriginalAmountAsAtLoadDate(getStringValue(rs, "OriginalAmountasatLoadDate"));
            entity.setEmzOutstandingBalance(getStringValue(rs, "EMZOutstandingBalance"));
            
            // Set audit fields
            entity.setCreatedBy(currentUser);
            entity.setUpdatedBy(currentUser);
            entity.setIsUpdated(false);
            entity.setValidationStatus("PENDING");
            
        } catch (SQLException e) {
            log.error("Error mapping ResultSet to CbcStagingRecordEntity: {}", e.getMessage());
            throw e;
        }
        
        return entity;
    }

    private String getStringValue(ResultSet rs, String columnName) {
        try {
            String value = rs.getString(columnName);
            return value != null ? value.trim() : null;
        } catch (SQLException e) {
            log.warn("Error getting string value for column '{}': {}", columnName, e.getMessage());
            return null;
        }
    }

    private BigDecimal getBigDecimalValue(ResultSet rs, String columnName) {
        try {
            Object value = rs.getObject(columnName);
            if (value == null) {
                return null;
            }
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            // Try to parse string value
            String stringValue = value.toString().trim();
            if (stringValue.isEmpty()) {
                return null;
            }
            return new BigDecimal(stringValue);
        } catch (Exception e) {
            log.warn("Error converting column '{}' to BigDecimal: {}", columnName, e.getMessage());
            return null;
        }
    }
}