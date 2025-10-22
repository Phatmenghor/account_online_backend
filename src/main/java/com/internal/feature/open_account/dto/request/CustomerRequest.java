package com.internal.feature.open_account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class CustomerRequest {
    
    @JsonProperty("Rec_ID")
    @NotBlank(message = "Record ID is required")
    private String recId;
    
    @JsonProperty("CompanyName")
    private String companyName;
    
    @JsonProperty("ReferalId")
    private String referalId;
    
    @JsonProperty("BranchCode")
    private String branchCode;
    
    @JsonProperty("FamilyName")
    @NotBlank(message = "Family name is required")
    private String familyName;
    
    @JsonProperty("GivenName")
    @NotBlank(message = "Given name is required")
    private String givenName;
    
    @JsonProperty("ShortName")
    private String shortName;
    
    @JsonProperty("firstNameKh")
    @NotBlank(message = "First name in Khmer is required")
    private String firstNameKh;
    
    @JsonProperty("lastNameKh")
    @NotBlank(message = "Last name in Khmer is required")
    private String lastNameKh;
    
    @JsonProperty("Gender")
    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(M|F|MALE|FEMALE)$", message = "Gender must be M, F, MALE, or FEMALE")
    private String gender;
    
    @JsonProperty("MaritalStatus")
    private String maritalStatus;
    
    @JsonProperty("DateOfBirth")
    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;
    
    @JsonProperty("PlaceOfBirth")
    private String placeOfBirth;
    
    @JsonProperty("Nationality")
    @NotBlank(message = "Nationality is required")
    private String nationality;
    
    @JsonProperty("AverageIncome")
    private String averageIncome;
    
    @JsonProperty("LegalDocName")
    @NotBlank(message = "Legal document name is required")
    private String legalDocName;
    
    @JsonProperty("LegalId")
    @NotBlank(message = "Legal ID is required")
    private String legalId;
    
    @JsonProperty("ReleasedBy")
    private String releasedBy;
    
    @JsonProperty("Occupation")
    private String occupation;
    
    @JsonProperty("CustProvince")
    private String custProvince;
    
    @JsonProperty("CustDistrict")
    private String custDistrict;
    
    @JsonProperty("CustCommune")
    private String custCommune;
    
    @JsonProperty("CustVillage")
    private String custVillage;
    
    @JsonProperty("Address")
    private String address;
    
    @JsonProperty("Sms")
    @NotBlank(message = "Phone number is required")
    private String sms;
    
    @JsonProperty("NID_IMAGE")
    @NotBlank(message = "NID image is required")
    private String nidImage;
    
    @JsonProperty("SELFIE_IMAGE")
    @NotBlank(message = "Selfie image is required")
    private String selfieImage;
}