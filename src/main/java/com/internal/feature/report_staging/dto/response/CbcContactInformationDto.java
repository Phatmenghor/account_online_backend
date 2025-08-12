package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcContactInformationDto {
    private Long id;
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
}