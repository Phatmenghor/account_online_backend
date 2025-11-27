package com.internal.feature.open_account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransmissionFormat {
    
    @JsonProperty("CustomerName")
    private String customerName;
    
    @JsonProperty("CustomerType")
    private String customerType;
    
    @JsonProperty("IdentityNumber")
    private String identityNumber;
    
    @JsonProperty("Email")
    private String email;
    
    @JsonProperty("Address")
    private String address;
    
    @JsonProperty("CifNo")
    private String cifNo;
    
    @JsonProperty("BranchCodeCreatedUser")
    private String branchCodeCreatedUser;
    
    @JsonProperty("PosCodeCreatedUser")
    private String posCodeCreatedUser;
    
    @JsonProperty("CreatedUser")
    private String createdUser;
    
    @JsonProperty("DateOfBirth")
    private String dateOfBirth;
    
    @JsonProperty("Telephone")
    private String telephone;
    
    @JsonProperty("CifBranchCode")
    private String cifBranchCode;
    
    @JsonProperty("Gender")
    private String gender;
    
    @JsonProperty("Residence")
    private String residence;
    
    @JsonProperty("AccountNumber")
    private String accountNumber;
    
    @JsonProperty("AccountType")
    private String accountType;
    
    @JsonProperty("Currency")
    private String currency;
    
    @JsonProperty("BranchCode")
    private String branchCode;
    
    @JsonProperty("PackageCode")
    private String packageCode;
    
    @JsonProperty("TelephoneOtp")
    private String telephoneOtp;
    
    @JsonProperty("StaffCode")
    private String staffCode;
    
    @JsonProperty("SignData")
    private String signData;
}
