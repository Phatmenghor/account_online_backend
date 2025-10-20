package com.internal.feature.camdx.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class CamdxValidateNidRequest {

    @JsonProperty("applicationName")
    @NotBlank(message = "Application name is required")
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
}