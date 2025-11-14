package com.internal.feature.aml.dto.response;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.dto.response.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmlStatusDto {

    private Long id;
    private String originalRequest;
    private String originalResponse;

    // CUSTOMER FIELDS (from CustomerAmlDto)
    private String legalId;
    private String familyName;
    private String givenName;
    private String firstNameKh;
    private String lastNameKh;
    private String dateOfBirth;
    private String placeOfBirth;
    private String gender;
    private String nationality;
    private String legalAddress;
    private String phoneNumber;

    // AML
    private AmlStatusEnum status;
    private String screeningResult;

    // AML EXTERNAL
    private String riskLevel;
    private String actionTaken;
    private Object[] rulesTriggered;
    private String serviceName;
    private int totalRulesScore;
    private String trxnID;

    // AUDIT
    private UserResponseDto approvedBy;
    private UserResponseDto rejectedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String currentAddressName;
    private String currentAddressCode;
    private String placeOfBirthName;
    private String placeOfBirthCode;
    private String maritalStatus;
    private String occupationCode;
    private String occupationStatus;
    private String issuedDate;
    private String expiredDate;

    private String remarks;
}
