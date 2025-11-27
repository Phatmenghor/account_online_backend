package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.dto.response.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAmlRequestDto {

    private AmlStatusEnum status;
    private UserResponseDto approvedBy;
    private UserResponseDto rejectedBy;

    private String idDisplay;
    private String familyName;
    private String givenName;
    private String firstNameKh;
    private String lastNameKh;
    private String dateOfBirth;
    private String gender;
    private String nationality;
    private String legalAddress;
}
