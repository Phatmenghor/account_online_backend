
package com.internal.feature.report_staging.mapper;

import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Slf4j
public class CbcResultSetMapper {

    public CbcStagingRecordEntity mapResultSetToStagingEntity(ResultSet rs, String currentUser) throws SQLException {
        CbcStagingRecordEntity entity = new CbcStagingRecordEntity();
        
        try {
            // Main Record Fields
            entity.setAccountNumber(getString(rs, "Account Number"));
            entity.setCreditorId(getString(rs, "CreditorID"));
            entity.setAccountType(getString(rs, "AccountType"));
            entity.setAsOfDate(getString(rs, "AsofDate"));
            
            // Personal Info Fields
            entity.setDateOfBirth(getString(rs, "DateofBirth"));
            entity.setFamilyNameEnglish(getString(rs, "FamilyName(English)"));
            entity.setFirstNameEnglish(getString(rs, "FirstName(English)"));
            entity.setSecondNameEnglish(getString(rs, "SecondName(English)"));
            entity.setThirdNameEnglish(getString(rs, "ThirdName(English)"));
            entity.setUnformattedNameEnglish(getString(rs, "UnformattedName(English)"));
            entity.setMothersNameUnformattedEnglish(getString(rs, "MothersNameUnformatted(English)"));
            entity.setFamilyNameKhmer(getString(rs, "FamilyName(Khmer)"));
            entity.setFirstNameKhmer(getString(rs, "FirstName(Khmer)"));
            entity.setSecondNameKhmer(getString(rs, "SecondName(Khmer)"));
            entity.setThirdNameKhmer(getString(rs, "ThirdName(Khmer)"));
            entity.setUnformattedNameKhmer(getString(rs, "UnformattedName(Khmer)"));
            entity.setMothersNameUnformattedKhmer(getString(rs, "MothersNameUnformatted(Khmer)"));
            entity.setGender(getString(rs, "Gender"));
            entity.setMaritalStatus(getString(rs, "MaritalStatus"));
            entity.setNationalityCode(getString(rs, "NationalityCode"));
            entity.setTaxpayerRegistrationNumber(getString(rs, "TaxpayerRegistrationNumber"));
            entity.setApplicantType(getString(rs, "ApplicantType"));
            
            // ID Information Fields
            entity.setIdType1(getString(rs, "IDType-1"));
            entity.setIdNumber1(getString(rs, "IDNumber-1"));
            entity.setIdExpiryDate1(getString(rs, "IDExpiryDate-1"));
            entity.setIdType2(getString(rs, "IDType-2"));
            entity.setIdNumber2(getString(rs, "IDNumber-2"));
            entity.setIdExpiryDate2(getString(rs, "IDExpiryDate-2"));
            entity.setIdType3(getString(rs, "IDType-3"));
            entity.setIdNumber3(getString(rs, "IDNumber-3"));
            entity.setIdExpiryDate3(getString(rs, "IDExpiryDate-3"));
            
            // Address Information Fields - Address 1
            entity.setAddressType1(getString(rs, "AddressType-1"));
            entity.setProvince1(getString(rs, "Province-1"));
            entity.setDistrict1(getString(rs, "District-1"));
            entity.setCommune1(getString(rs, "Commune-1"));
            entity.setVillage1(getString(rs, "Village-1"));
            entity.setAddress1Field1English(getString(rs, "Address-1Field1(English)"));
            entity.setAddress1Field2English(getString(rs, "Address-1Field2(English)"));
            entity.setAddress1Field1Khmer(getString(rs, "Address-1Field1(Khmer)"));
            entity.setAddress1Field2Khmer(getString(rs, "Address-1Field2(Khmer)"));
            entity.setCity1English(getString(rs, "City-1(English)"));
            entity.setCity1Khmer(getString(rs, "City-1(Khmer)"));
            entity.setCountry1(getString(rs, "Country-1"));
            entity.setPostalCode1(getString(rs, "PostalCode-1"));
            
            // Address 2
            entity.setAddressType2(getString(rs, "AddressType-2"));
            entity.setProvince2(getString(rs, "Province-2"));
            entity.setDistrict2(getString(rs, "District-2"));
            entity.setCommune2(getString(rs, "Commune-2"));
            entity.setVillage2(getString(rs, "Village-2"));
            entity.setAddress2Field1English(getString(rs, "Address-2Field1(English)"));
            entity.setAddress2Field2English(getString(rs, "Address-2Field2(English)"));
            entity.setAddress2Field1Khmer(getString(rs, "Address-2Field1(Khmer)"));
            entity.setAddress2Field2Khmer(getString(rs, "Address-2Field2(Khmer)"));
            entity.setCity2English(getString(rs, "City-2(English)"));
            entity.setCity2Khmer(getString(rs, "City-2(Khmer)"));
            entity.setCountry2(getString(rs, "Country-2"));
            entity.setPostalCode2(getString(rs, "PostalCode-2"));
            
            // Address 3
            entity.setAddress3Type(getString(rs, "Address-3Type"));
            entity.setProvince3(getString(rs, "Province-3"));
            entity.setDistrict3(getString(rs, "District-3"));
            entity.setCommune3(getString(rs, "Commune-3"));
            entity.setVillage3(getString(rs, "Village-3"));
            entity.setAddress3Field1English(getString(rs, "Address-3Field1(English)"));
            entity.setAddress3Field2English(getString(rs, "Address-3Field2(English)"));
            entity.setAddress3Field1Khmer(getString(rs, "Address-3Field1(Khmer)"));
            entity.setAddress3Field2Khmer(getString(rs, "Address-3Field2(Khmer)"));
            entity.setCity3English(getString(rs, "City-3(English)"));
            entity.setCity3Khmer(getString(rs, "City-3(Khmer)"));
            entity.setCountry3(getString(rs, "Country-3"));
            entity.setPostalCode3(getString(rs, "PostalCode-3"));
            
            // Contact Information Fields
            entity.setEmailAddress(getString(rs, "EmailAddress"));
            entity.setContactNumberType1(getString(rs, "ContactNumberType1"));
            entity.setContactNumberCountryCode1(getString(rs, "ContactNumber–CountryCode1"));
            entity.setContactNumberArea1(getString(rs, "ContactNumber–Area1"));
            entity.setContactNumberNumber1(getString(rs, "ContactNumber–Number1"));
            entity.setContactNumberExtension1(getString(rs, "ContactNumber–Extension1"));
            entity.setContactNumberType2(getString(rs, "ContactNumberType2"));
            entity.setContactNumberCountryCode2(getString(rs, "ContactNumber–CountryCode2"));
            entity.setContactNumberArea2(getString(rs, "ContactNumber–Area2"));
            entity.setContactNumberNumber2(getString(rs, "ContactNumber–Number2"));
            entity.setContactNumberExtension2(getString(rs, "ContactNumber–Extension2"));
            entity.setContactNumberType3(getString(rs, "ContactNumberType3"));
            entity.setContactNumberCountryCode3(getString(rs, "ContactNumber–CountryCode3"));
            entity.setContactNumberArea3(getString(rs, "ContactNumber–Area3"));
            entity.setContactNumberNumber3(getString(rs, "ContactNumber–Number3"));
            entity.setContactNumberExtension3(getString(rs, "ContactNumber–Extension3"));
            
            // Employment Information Fields - Employment 1
            entity.setEmploymentStatus1(getString(rs, "Employment Status-1"));
            entity.setEmploymentType1(getString(rs, "Employment Type-1"));
            entity.setEmployer1NameEnglish(getString(rs, "Employer-1Name(English)"));
            entity.setEmployer1NameKhmer(getString(rs, "Employer-1Name(Khmer)"));
            entity.setEconomicSector1(getString(rs, "EconomicSector-1"));
            entity.setBusinessType1(getString(rs, "BusinessType-1"));
            entity.setEmployer1AddressEnglish(getString(rs, "Employer-1'sAddress(English)"));
            entity.setEmployer1AddressKhmer(getString(rs, "Employer-1'sAddress(Khmer)"));
            entity.setEmployer1Province(getString(rs, "Employer-1'sProvince"));
            entity.setEmployer1District(getString(rs, "Employer-1'sDistrict"));
            entity.setEmployer1Commune(getString(rs, "Employer-1'sCommune"));
            entity.setEmployer1Village(getString(rs, "Employer-1'sVillage"));
            entity.setEmployer1AddressCityEnglish(getString(rs, "Employer-1'sAddressCity(English)"));
            entity.setEmployer1AddressCityKhmer(getString(rs, "Employer-1'sAddressCity(Khmer)"));
            entity.setEmp1Country1(getString(rs, "EMP1Country-1"));
            entity.setEmp1PostalCode1(getString(rs, "EMP1PostalCode-1"));
            entity.setOccupation1English(getString(rs, "Occupation-1(English)"));
            entity.setOccupation1Khmer(getString(rs, "Occupation-1(Khmer)"));
            entity.setDateOfEmployment1(getString(rs, "DateofEmployment-1"));
            entity.setLengthOfService1Months(getString(rs, "LengthofService-1(Months)"));
            entity.setContractExpiryDate1(getString(rs, "ContractExpiryDate-1"));
            entity.setCurrency1(getString(rs, "Currency-1"));
            entity.setMonthlyBasicSalaryIncome1(getBigDecimal(rs, "MonthlyBasicSalary/Income-1"));
            entity.setTotalMonthlySalaryIncome1(getBigDecimal(rs, "TotalMonthlySalary/Income-1"));
            
            // Employment 2
            entity.setEmployerType2(getString(rs, "Employer-2Type"));
            entity.setSelfEmployed2(getString(rs, "SelfEmployed-2"));
            entity.setEmployer2NameEnglish(getString(rs, "Employer-2Name(English)"));
            entity.setEmployer2NameKhmer(getString(rs, "Employer-2Name(Khmer)"));
            entity.setEconomicSector2(getString(rs, "EconomicSector-2"));
            entity.setBusinessType2(getString(rs, "BusinessType-2"));
            entity.setEmployer2AddressEnglish(getString(rs, "Employer-2'sAddress(English)"));
            entity.setEmployer2AddressKhmer(getString(rs, "Employer-2'sAddress(Khmer)"));
            entity.setEmployer2Province(getString(rs, "Employer-2'sProvince"));
            entity.setEmployer2District(getString(rs, "Employer-2'sDistrict"));
            entity.setEmployer2Commune(getString(rs, "Employer-2'sCommune"));
            entity.setEmployer2Village(getString(rs, "Employer-2'sVillage"));
            entity.setEmployer2AddressCityEnglish(getString(rs, "Employer-2'sAddressCity(English)"));
            entity.setEmployer2AddressCityKhmer(getString(rs, "Employer-2'sAddressCity(Khmer)"));
            entity.setEmzCountry2(getString(rs, "EMZCountry-2"));
            entity.setEmzPostalCode2(getString(rs, "EMZPostalCode-2"));
            entity.setOccupation2English(getString(rs, "Occupation-2(English)"));
            entity.setOccupation2Khmer(getString(rs, "Occupation-2(Khmer)"));
            entity.setDateOfEmployment2(getString(rs, "DateofEmployment-2"));
            entity.setLengthOfService2Months(getString(rs, "LengthofService-2(Months)"));
            entity.setContractExpiryDate2(getString(rs, "ContractExpiryDate-2"));
            entity.setCurrency2(getString(rs, "Currency–2"));
            entity.setMonthlyBasicSalaryIncome2(getBigDecimal(rs, "MonthlyBasicSalary/Income-2"));
            entity.setTotalMonthlySalaryIncome2(getBigDecimal(rs, "TotalMonthlySalary/Income-2"));
            
            // Employment 3
            entity.setEmployerType3(getString(rs, "EmployerType-3"));
            entity.setSelfEmployed3(getString(rs, "SelfEmployed-3"));
            entity.setEmployer3NameEnglish(getString(rs, "Employer-3Name(English)"));
            entity.setEmployer3NameKhmer(getString(rs, "Employer-3Name(Khmer)"));
            entity.setEconomicSector3(getString(rs, "EconomicSector-3"));
            entity.setBusinessType3(getString(rs, "BusinessType-3"));
            entity.setEmployer3AddressEnglish(getString(rs, "Employer-3'sAddress(English)"));
            entity.setEmployer3AddressKhmer(getString(rs, "Employer-3'sAddress(Khmer)"));
            entity.setEmployer3Province(getString(rs, "Employer-3'sProvince"));
            entity.setEmployer3District(getString(rs, "Employer-3'sDistrict"));
            entity.setEmployer3Commune(getString(rs, "Employer-3'sCommune"));
            entity.setEmployer3Village(getString(rs, "Employer-3'sVillage"));
            entity.setEmployer3AddressCityEnglish(getString(rs, "Employer-3'sAddressCity(English)"));
            entity.setEmployer3AddressCityKhmer(getString(rs, "Employer-3'sAddressCity(Khmer)"));
            entity.setEmp3Country3(getString(rs, "EMP3Country-3"));
            entity.setEmp3PostalCode3(getString(rs, "EMP3PostalCode-3"));
            entity.setOccupation3English(getString(rs, "Occupation-3(English)"));
            entity.setOccupation3Khmer(getString(rs, "Occupation-3(Khmer)"));
            entity.setDateOfEmployment3(getString(rs, "DateofEmployment-3"));
            entity.setLengthOfService3Months(getString(rs, "LengthofService-3(Months)"));
            entity.setContractExpiryDate3(getString(rs, "ContractExpiryDate-3"));
            entity.setCurrency3(getString(rs, "Currency-3"));
            entity.setMonthlyBasicSalaryIncome3(getBigDecimal(rs, "MonthlyBasicSalary/Income-3"));
            entity.setTotalMonthlySalaryIncome3(getBigDecimal(rs, "TotalMonthlySalary/Income-3"));
            
            // Security Information Fields
            entity.setSecurityType1(getString(rs, "Security Type-1"));
            entity.setSecurityNumber1(getString(rs, "Security Number-1"));
            entity.setSecurityCurrency1(getString(rs, "Security Currency-1"));
            entity.setSecurityValue1(getBigDecimal(rs, "Security Value-1"));
            entity.setSecurityLocation1(getString(rs, "Security Location-1"));
            entity.setSecurityType2(getString(rs, "Security Type-2"));
            entity.setSecurityNumber2(getString(rs, "Security Number-2"));
            entity.setSecurityCurrency2(getString(rs, "Security Currency-2"));
            entity.setSecurityValue2(getBigDecimal(rs, "Security Value-2"));
            entity.setSecurityLocation2(getString(rs, "Security Location-2"));
            entity.setSecurityType3(getString(rs, "Security Type-3"));
            entity.setSecurityNumber3(getString(rs, "Security Number-3"));
            entity.setSecurityCurrency3(getString(rs, "Security Currency-3"));
            entity.setSecurityValue3(getBigDecimal(rs, "Security Value-3"));
            entity.setSecurityLocation3(getString(rs, "Security Location-3"));
            entity.setSecurityTypePrimary(getString(rs, "SecurityType-Primary"));
            entity.setSpecialNote(getString(rs, "Special Note"));
            entity.setEnquiryMemberReference(getString(rs, "Enquiry Member Reference"));
            entity.setLoanToSectorSection(getString(rs, "Loan to sector/Section"));
            entity.setBranchAddressCode(getString(rs, "Branch Address Code"));
            
            // Loan Information Fields
            entity.setLoanTermType(getString(rs, "Loan Term Type"));
            entity.setGroupAccountReference(getString(rs, "Group Account Reference"));
            entity.setDateIssued(getString(rs, "Date Issued"));
            entity.setProductType(getString(rs, "ProductType"));
            entity.setCurrency(getString(rs, "Currency"));
            entity.setProductLimitOriginalAmount(getBigDecimal(rs, "ProductLimit/OriginalAmount"));
            entity.setProductExpiryDate(getString(rs, "ProductExpiryDate"));
            entity.setProductStatus(getString(rs, "ProductStatus"));
            entity.setRestructuredLoan(getString(rs, "Restructured Loan"));
            entity.setInstalmentAmount(getBigDecimal(rs, "InstalmentAmount"));
            entity.setPaymentFrequency(getString(rs, "PaymentFrequency"));
            entity.setTenure(getString(rs, "Tenure"));
            entity.setLastPaymentDate(getString(rs, "LastPaymentDate"));
            entity.setLastAmountPaid(getBigDecimal(rs, "LastAmountPaid"));
            entity.setOutstandingBalance(getBigDecimal(rs, "OutstandingBalance"));
            entity.setPastDue(getBigDecimal(rs, "PastDue"));
            entity.setNextPaymentDate(getString(rs, "NextPaymentDate"));
            entity.setPaymentStatusCode(getString(rs, "PaymentStatusCode"));
            entity.setLossStatus(getString(rs, "LossStatus"));
            entity.setLossStatusDate(getString(rs, "LossStatusDate"));
            entity.setOriginalAmountAsAtLoadDate(getString(rs, "OriginalAmountasatLoadDate"));
            entity.setEmzOutstandingBalance(getString(rs, "EMZOutstandingBalance"));
            
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

    private String getString(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            log.warn("Error getting string value for column {}: {}", columnName, e.getMessage());
            return null;
        }
    }

    private BigDecimal getBigDecimal(ResultSet rs, String columnName) {
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