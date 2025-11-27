package com.internal.feature.camdx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CamdxUserInfoRequest {

    @JsonProperty("applicationName")
    private String applicationName;

    @JsonProperty("idNumber")
    private String idNumber;

    @JsonProperty("firstNameKh")
    private String firstNameKh;

    @JsonProperty("lastNameKh")
    private String lastNameKh;

    @JsonProperty("firstNameEn")
    private String firstNameEn;

    @JsonProperty("lastNameEn")
    private String lastNameEn;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("issuedDate")
    private String issuedDate;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("expiredDate")
    private String expiredDate;
}
