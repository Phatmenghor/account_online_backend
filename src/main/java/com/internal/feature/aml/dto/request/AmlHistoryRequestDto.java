package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.dto.response.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmlHistoryRequestDto {
    private String originalRequest;
    private String originalResponse;
    private AmlStatusEnum oldStatus;
    private AmlStatusEnum newStatus;
    private UserResponseDto changedBy;

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
