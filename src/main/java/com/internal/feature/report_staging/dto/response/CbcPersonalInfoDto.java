package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcPersonalInfoDto {
    private Long id;
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
}