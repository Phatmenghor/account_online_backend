package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcRecordResponseDto {
    private UUID id;
    private String recordType; // "STAGING" or "FINAL"
    
    // Batch info (only for final records)
    private String batchSessionId;
    private LocalDate batchSessionDate;
    private LocalDateTime processedDate;
    private LocalDate originalLoadDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Main Record Fields
    private String accountNumber;
    private String creditorId;
    private String accountType;
    private String asOfDate;

    // Personal Info Fields
    private String dateOfBirth;
    private String familyNameEnglish;
    private String firstNameEnglish;
    private String secondNameEnglish;
    private String thirdNameEnglish;
    private String unformattedNameEnglish;
    private String mothersNameUnformattedEnglish;
    private String familyNameKhmer;
    private String firstNameKhmer;
    private String secondNameKhmer;
    private String thirdNameKhmer;
    private String unformattedNameKhmer;
    private String mothersNameUnformattedKhmer;
    private String gender;
    private String maritalStatus;
    private String nationalityCode;
    private String taxpayerRegistrationNumber;
    private String applicantType;

    // ID Information Fields
    private String idType1;
    private String idNumber1;
    private String idExpiryDate1;
    private String idType2;
    private String idNumber2;
    private String idExpiryDate2;
    private String idType3;
    private String idNumber3;
    private String idExpiryDate3;

    // Address Information Fields - Address 1
    private String addressType1;
    private String province1;
    private String district1;
    private String commune1;
    private String village1;
    private String address1Field1English;
    private String address1Field2English;
    private String address1Field1Khmer;
    private String address1Field2Khmer;
    private String city1English;
    private String city1Khmer;
    private String country1;
    private String postalCode1;

    // Address 2
    private String addressType2;
    private String province2;
    private String district2;
    private String commune2;
    private String village2;
    private String address2Field1English;
    private String address2Field2English;
    private String address2Field1Khmer;
    private String address2Field2Khmer;
    private String city2English;
    private String city2Khmer;
    private String country2;
    private String postalCode2;

    // Address 3
    private String address3Type;
    private String province3;
    private String district3;
    private String commune3;
    private String village3;
    private String address3Field1English;
    private String address3Field2English;
    private String address3Field1Khmer;
    private String address3Field2Khmer;
    private String city3English;
    private String city3Khmer;
    private String country3;
    private String postalCode3;

    // Contact Information Fields
    private String emailAddress;
    private String contactNumberType1;
    private String contactNumberCountryCode1;
    private String contactNumberArea1;
    private String contactNumberNumber1;
    private String contactNumberExtension1;
    private String contactNumberType2;
    private String contactNumberCountryCode2;
    private String contactNumberArea2;
    private String contactNumberNumber2;
    private String contactNumberExtension2;
    private String contactNumberType3;
    private String contactNumberCountryCode3;
    private String contactNumberArea3;
    private String contactNumberNumber3;
    private String contactNumberExtension3;

    // Employment Information Fields - Employment 1
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
    private String employer3NameKhmer;
    private String economicSector3;
    private String businessType3;
    private String employer3AddressEnglish;
    private String employer3AddressKhmer;
    private String employer3Province;
    private String employer3District;
    private String employer3Commune;
    private String employer3Village;
    private String employer3AddressCityEnglish;
    private String employer3AddressCityKhmer;
    private String emp3Country3;
    private String emp3PostalCode3;
    private String occupation3English;
    private String occupation3Khmer;
    private String dateOfEmployment3;
    private String lengthOfService3Months;
    private String contractExpiryDate3;
    private String currency3;
    private BigDecimal monthlyBasicSalaryIncome3;
    private BigDecimal totalMonthlySalaryIncome3;

    // Security Information Fields
    private String securityType1;
    private String securityNumber1;
    private String securityCurrency1;
    private BigDecimal securityValue1;
    private String securityLocation1;

    private String securityType2;
    private String securityNumber2;
    private String securityCurrency2;
    private BigDecimal securityValue2;
    private String securityLocation2;

    private String securityType3;
    private String securityNumber3;
    private String securityCurrency3;
    private BigDecimal securityValue3;
    private String securityLocation3;

    private String securityTypePrimary;
    private String specialNote;
    private String enquiryMemberReference;
    private String loanToSectorSection;
    private String branchAddressCode;

    // Loan Information Fields
    private String loanTermType;
    private String groupAccountReference;
    private String dateIssued;
    private String productType;
    private String currency;
    private BigDecimal productLimitOriginalAmount;
    private String productExpiryDate;
    private String productStatus;
    private String restructuredLoan;
    private BigDecimal instalmentAmount;
    private String paymentFrequency;
    private String tenure;
    private String lastPaymentDate;
    private BigDecimal lastAmountPaid;
    private BigDecimal outstandingBalance;
    private BigDecimal pastDue;
    private String nextPaymentDate;
    private String paymentStatusCode;
    private String lossStatus;
    private String lossStatusDate;
    private String originalAmountAsAtLoadDate;
    private String emzOutstandingBalance;

    // Status tracking
    private Boolean isUpdated;
    private Boolean wasUpdated;
    private String validationStatus;
    private String validationErrors;
    private Integer updateCount;
    private LocalDateTime lastUpdatedDate;
    private String archiveStatus;
}