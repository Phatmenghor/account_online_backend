package com.internal.feature.logs_report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineFinalResponseDto {
    private UUID id;
    private String cif;
    private String legalId;
    private String legalFirstNameEn;
    private String legalLastNameEn;
    private String legalFirstNameKh;
    private String legalLastNameKh;
    private LocalDate legalDateOfBirth;
    private String legalGender;
    private String legalAddress;
    private String legalPlaceOfBirth;
    private String maritalStatus;
    private String nationality;
    private String companyName;
    private String occupation;
    private String phoneNumber;
    private String nidImage;
    private String selfieImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
