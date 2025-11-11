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
}

