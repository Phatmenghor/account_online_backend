package com.internal.feature.camdx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CamdxValidateNidRequest {

    @JsonProperty("applicationName")
    private String applicationName;

    @JsonProperty("idNumber")
    private String idNumber;

    @JsonProperty("lastNameKh")
    private String lastNameKh;

    @JsonProperty("firstNameKh")
    private String firstNameKh;

    @JsonProperty("lastNameEn")
    private String lastNameEn;

    @JsonProperty("firstNameEn")
    private String firstNameEn;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("expiredDate")
    private String expiredDate;

    @JsonProperty("issuedDate")
    private String issuedDate;

    @JsonProperty(value = "phoneNumber", access = JsonProperty.Access.WRITE_ONLY)
    private String phoneNumber;
}
