package com.internal.feature.open_account.dto.request;

import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class CustomerRequest {

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

    @NotBlank(message = "Gender is required")
    private String gender;
    private String placeOfBirth;

    private String companyName;

    //Referral By is input staff code
    private String referralId;
    private String branchCode;

    private String occupation;
    private String maritalStatus;

    private String customerCurrentProvince;
    private String customerCurrentDistrict;
    private String customerCurrentCommune;
    private String customerCurrentVillage;

    private String customerPobProvince;
    private String customerPobDistrict;
    private String customerPobCommune;
    private String customerPobVillage;

    @NotBlank(message = "Legal ID is required")
    private String legalId;

    //legal type
    private String legalIssueDate;
    private String legalExpireDate;
    private String legalAddress;
    private String legalDocType;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "NID image is required")
    private String nidImage;
    
    @NotBlank(message = "Selfie image is required")
    private String selfieImage;
}