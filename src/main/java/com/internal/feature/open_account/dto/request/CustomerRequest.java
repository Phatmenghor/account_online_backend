package com.internal.feature.open_account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class CustomerRequest {
    
    @NotBlank(message = "Record ID is required")
    private String recId;

    @JsonProperty("LegalId")
    @NotBlank(message = "Legal ID is required")
    private String legalId;

    @JsonProperty("FamilyName")
    @NotBlank(message = "Family name is required")
    private String familyName;
    @NotBlank(message = "Given name is required")
    private String givenName;

    @JsonProperty("firstNameKh")
    @NotBlank(message = "First name in Khmer is required")
    private String firstNameKh;
    @JsonProperty("lastNameKh")
    @NotBlank(message = "Last name in Khmer is required")
    private String lastNameKh;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    private String legalAddress;
    
    @JsonProperty("Gender")
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
    
    @JsonProperty("AverageIncome")
    private String averageIncome;

    @JsonProperty("LegalDocName")
    @NotBlank(message = "Legal document name is required")
    private String legalDocName;
    
    @JsonProperty("Occupation")
    private String occupation;
    
    private String customerProvince;
    private String customerDistrict;
    private String customerCommune;
    private String customerVillage;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @JsonProperty("NID_IMAGE")
    @NotBlank(message = "NID image is required")
    private String nidImage;
    
    @JsonProperty("SELFIE_IMAGE")
    @NotBlank(message = "Selfie image is required")
    private String selfieImage;
}