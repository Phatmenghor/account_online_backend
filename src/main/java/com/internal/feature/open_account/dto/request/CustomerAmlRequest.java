package com.internal.feature.open_account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerAmlRequest {

    @JsonProperty("CUSTOMER_ID")
    private String customerId;

    @JsonProperty("CUST_CREATE_DATE")
    private String custCreateDate;

    @JsonProperty("CUSTOMER_TYPE")
    private String customerType;

    @JsonProperty("CUST_NAME")
    private String custName;

    @JsonProperty("GIVEN_NAME")
    private String givenName;

    @JsonProperty("FAMILY_NAME")
    private String familyName;

    @JsonProperty("GENDER")
    private String gender;

    @JsonProperty("DATE_OF_BIRTH")
    private String dateOfBirth;

    @JsonProperty("NATIONALITY")
    private String nationality;

    @JsonProperty("ADDRESS")
    private String legalAddress;

    @JsonProperty("CUST_DISTRICT")
    private String custDistrict;

    @JsonProperty("CUST_PROVINCE")
    private String custProvince;

    @JsonProperty("COUNTRY")
    private String country;

    @JsonProperty("SMS_1")
    private String sms1;

    @JsonProperty("PHONE_1")
    private String phoneNumber;

    @JsonProperty("OFF_PHONE")
    private String offPhone;

    @JsonProperty("OCCUPATION")
    private String occupation;

    @JsonProperty("LEGAL_ID")
    private String legalId;

    @JsonProperty("MARITAL_STATUS")
    private String maritalStatus;

    @JsonProperty("BUSINESS_SECTOR")
    private String businessSector;

    @JsonProperty("TARGET")
    private String target;

    @JsonProperty("INCOME")
    private Integer income;

    @JsonProperty("DOBYear")
    private Integer dobYear;

    @JsonProperty("DOBMonth")
    private Integer dobMonth;

    @JsonProperty("DOBDay")
    private Integer dobDay;

    @JsonProperty("LEGAL_DOC_NAME")
    private String legalDocName;

    @JsonProperty("LEGAL_EXP_DATE")
    private String legalExpDate;

    @JsonProperty("CUSTOMER_RATING")
    private String customerRating;
}
