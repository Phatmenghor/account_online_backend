package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcIdInformationDto {
    private Long id;
    private String idType1;
    private String idNumber1;
    private String idExpiryDate1;
    private String idType2;
    private String idNumber2;
    private String idExpiryDate2;
    private String idType3;
    private String idNumber3;
    private String idExpiryDate3;
}