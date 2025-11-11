package com.internal.feature.open_account.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class CustomerRequest {

    @NotBlank(message = "Legal ID is required")
    private String legalId;

    @NotBlank(message = "Family name is required")
    private String familyName;
    @NotBlank(message = "Given name is required")
    private String givenName;

    @NotBlank(message = "First name in Khmer is required")
    private String firstNameKh;
    @NotBlank(message = "Last name in Khmer is required")
    private String lastNameKh;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    private String legalAddress;
    
    @NotBlank(message = "Gender is required")
    private String gender;
    
    private String maritalStatus;
    private String companyName;
    private String referralId;
    private String branchCode;
    private String placeOfBirth;
    @NotBlank(message = "Nationality is required")
    private String nationality;

    private String releasedBy;
    
    private String averageIncome;

    private String legalDocName;
    
    private String occupation;
    
    private String customerProvince;
    private String customerDistrict;
    private String customerCommune;
    private String customerVillage;

    private String legalIssueDate;

    private String legalExpireDate;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "NID image is required")
    private String nidImage;
    
    @NotBlank(message = "Selfie image is required")
    private String selfieImage;
}