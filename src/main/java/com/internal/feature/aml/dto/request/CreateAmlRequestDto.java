package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.dto.response.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAmlRequestDto {

    private String originalRequest;
    private String originalResponse;
    private AmlStatusEnum status;
    private String screeningResult;
    private String RiskLevel;
    private String ActionTaken;
    private Object[] RulesTriggered;
    private String ServiceName;
    private int TotalRulesScore;
    private String TrxnID;
    private UserResponseDto approvedBy;
    private UserResponseDto rejectedBy;

    private String legalId;
    private String familyName;
    private String givenName;
    private String firstNameKh;
    private String lastNameKh;
    private String dateOfBirth;
    private String gender;
    private String nationality;
    private String legalAddress;

    private String customerCurrentProvince;
    private String customerCurrentDistrict;
    private String customerCurrentCommune;
    private String customerCurrentVillage;

    private String customerPobProvince;
    private String customerPobDistrict;
    private String customerPobCommune;
    private String customerPobVillage;
}

