package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcSecurityInformationDto {
    private Long id;
    
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
    private String currency3;
    private String branchAddressCode;
}