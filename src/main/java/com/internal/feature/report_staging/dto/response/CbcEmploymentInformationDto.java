package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcEmploymentInformationDto {
    private Long id;
    
    // Employment 1
    private String employmentStatus1;
    private String employmentType1;
    private String employer1NameEnglish;
    private String employer1NameKhmer;
    private String economicSector1;
    private String businessType1;
    private String employer1AddressEnglish;
    private String employer1AddressKhmer;
    private String employer1Province;
    private String employer1District;
    private String employer1Commune;
    private String employer1Village;
    private String employer1AddressCityEnglish;
    private String employer1AddressCityKhmer;
    private String emp1Country1;
    private String emp1PostalCode1;
    private String occupation1English;
    private String occupation1Khmer;
    private String dateOfEmployment1;
    private String lengthOfService1Months;
    private String contractExpiryDate1;
    private String currency1;
    private BigDecimal monthlyBasicSalaryIncome1;
    private BigDecimal totalMonthlySalaryIncome1;
    
    // Employment 2
    private String employerType2;
    private String selfEmployed2;
    private String employer2NameEnglish;
    private String employer2NameKhmer;
    private String economicSector2;
    private String businessType2;
    private String employer2AddressEnglish;
    private String employer2AddressKhmer;
    private String employer2Province;
    private String employer2District;
    private String employer2Commune;
    private String employer2Village;
    private String employer2AddressCityEnglish;
    private String employer2AddressCityKhmer;
    private String emzCountry2;
    private String emzPostalCode2;
    private String occupation2English;
    private String occupation2Khmer;
    private String dateOfEmployment2;
    private String lengthOfService2Months;
    private String contractExpiryDate2;
    private String currency2;
    private BigDecimal monthlyBasicSalaryIncome2;
    private BigDecimal totalMonthlySalaryIncome2;
    
    // Employment 3
    private String employerType3;
    private String selfEmployed3;
    private String employer3NameEnglish;
}