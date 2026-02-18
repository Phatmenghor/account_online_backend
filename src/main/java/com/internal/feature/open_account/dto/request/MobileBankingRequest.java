package com.internal.feature.open_account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileBankingRequest {

    @JsonProperty("CifNo")
    private String cifNo;

    @JsonProperty("CifBranch")
    private String cifBranchCode;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("CusName")
    private String customerName;

    @JsonProperty("CusType")
    private String customerType;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("BirthDay")
    private String dateOfBirth;

    @JsonProperty("IdNumber")
    private String identityNumber;

    @JsonProperty("Residence")
    private String residence;

    @JsonProperty("CreatedUser")
    private String createdUser;

    @JsonProperty("BranchCodeCreatedUser")
    private String branchCodeCreatedUser;

    @JsonProperty("PosCodeCreatedUser")
    private String posCodeCreatedUser;

    @JsonProperty("StaffCode")
    private String staffCode;

    @JsonProperty("AccountNo")
    private String accountNumber;

    @JsonProperty("AccountType")
    private String accountType;

    @JsonProperty("Ccy")
    private String currency;

    @JsonProperty("BranchCode")
    private String branchCode;

    @JsonProperty("Telephone")
    private String telephone;

    @JsonProperty("TelephoneOtp")
    private String telephoneOtp;

    @JsonProperty("PackageCode")
    private String packageCode;

    @JsonProperty("SignData")
    private String signData;

    @JsonProperty("Channel")
    private String channel;

    @JsonProperty("MobileChannel")
    private String mobileChannel;
}
