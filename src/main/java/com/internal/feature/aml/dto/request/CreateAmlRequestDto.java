package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.dto.response.UserResponseDto;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAmlRequestDto {

    // Original request/response
    private String originalRequest;
    private String originalResponse;

    // AML status
    private AmlStatusEnum status;

    // CUSTOMER INFO
    private String legalId;
    private String familyName;
    private String givenName;
    private String firstNameKh;
    private String lastNameKh;
    private String dateOfBirth;
    private String gender;
    private String nationality;

    // Contact / Personal
    private String phoneNumber;
    private String maritalStatus;

    // Document
    private String issuedDate;
    private String expiredDate;

    // Address
    private String legalAddress;

    private String customerCurrentProvince;
    private String customerCurrentDistrict;
    private String customerCurrentCommune;
    private String customerCurrentVillage;

    private String customerPobProvince;
    private String customerPobDistrict;
    private String customerPobCommune;
    private String customerPobVillage;

    // Occupation
    private String occupationCode;
    private String occupationStatus;

    // AML SCREENING
    private String screeningResult;
    private String riskLevel;
    private String actionTaken;
    private String rulesTriggered;
    private String serviceName;
    private Integer totalRulesScore;
    private String trxnID;

    // Admin workflow
    private UserResponseDto approvedBy;
    private UserResponseDto rejectedBy;
}
