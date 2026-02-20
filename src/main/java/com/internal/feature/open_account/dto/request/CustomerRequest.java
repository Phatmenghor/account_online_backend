package com.internal.feature.open_account.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRequest {

    @JsonProperty("rec_id")
    private String recId;

    @JsonProperty("branch_code")
    private String branchCode;

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("fullname1")
    private String fullName1;

    @JsonProperty("fullname2")
    private String fullName2;

    @JsonProperty("sector")
    private String sector;

    @JsonProperty("cost_center")
    private String costCenter;

    @JsonProperty("industry")
    private String industry;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("customer_status")
    private String customerStatus;

    @JsonProperty("residence")
    private String residence;

    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Legal ID is required")
    @JsonProperty("legal_id")
    private String legalId;

    @JsonProperty("legal_doc_name")
    private String legalDocType;

    @JsonProperty("legal_holder_name")
    private String legalHolderName;

    @JsonProperty("legal_iss_auth")
    private String legalIssAuth;

    @JsonProperty("legal_iss_date")
    private String legalIssueDate;

    @JsonProperty("legal_exp_date")
    private String legalExpireDate;

    @JsonProperty("address")
    private String legalAddress;

    @JsonProperty("language")
    private String language;

    @JsonProperty("customer_rate")
    private String customerRate;

    @JsonProperty("title")
    private String title;

    @NotBlank(message = "Given name is required")
    @JsonProperty("given_name")
    private String givenName;

    @NotBlank(message = "Family name is required")
    @JsonProperty("family_name")
    private String familyName;

    @NotBlank(message = "Gender is required")
    @JsonProperty("gender")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("marital_status")
    private String maritalStatus;

    @NotBlank(message = "Phone number is required")
    @JsonProperty("sms")
    private String phoneNumber;

    @JsonProperty("customer_type")
    private String customerType;

    @JsonProperty("cust_province")
    private String customerCurrentProvince;

    @JsonProperty("cust_district")
    private String customerCurrentDistrict;

    @JsonProperty("cust_commune")
    private String customerCurrentCommune;

    @JsonProperty("cust_village")
    private String customerCurrentVillage;

    @JsonProperty("cust_pob_province")
    private String customerPobProvince;

    @JsonProperty("cust_pob_district")
    private String customerPobDistrict;

    @JsonProperty("cust_pob_commune")
    private String customerPobCommune;

    @JsonProperty("cust_pob_village")
    private String customerPobVillage;

    @JsonProperty("ownership")
    private String ownership;

    @JsonProperty("loan_officer")
    private String loanOfficer;

    @JsonProperty("staff")
    private String staff;

    @JsonProperty("target")
    private String target;

    @JsonProperty("average_income")
    private String averageIncome;

    @JsonProperty("occupation")
    private String occupation;

    @JsonProperty("company")
    private String companyName;

    @JsonProperty("staff_code")
    private String referralId;

    @JsonProperty("released_by")
    private String releasedBy;

    @JsonProperty("nid_image_name")
    private String nidImageName; // Filename from upload

    @JsonProperty("selfie_image_name")
    private String selfieImageName; // Filename from upload

    private String legalMrz1;
    private String legalMrz2;
    private String legalMrz3;

    @JsonProperty("product_account")
    private String productAccount;

    @JsonProperty("category_account")
    private String categoryAccount;

    @JsonProperty("customer_role")
    private String customerRole;

    @JsonProperty("referral_by")
    private String referralBy;

    @JsonProperty("relation_manager")
    private String relationManager;

    @JsonProperty("place_of_birth")
    private String placeOfBirth;

    @JsonProperty("firstNameKh")
    @NotBlank(message = "First name in Khmer is required")
    private String firstNameKh;

    @JsonProperty("lastNameKh")
    @NotBlank(message = "Last name in Khmer is required")
    private String lastNameKh;
}
