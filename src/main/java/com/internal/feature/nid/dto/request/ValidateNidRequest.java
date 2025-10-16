package com.internal.feature.nid.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ValidateNidRequest {

    @JsonProperty("applicationName")
    @NotBlank
    private String applicationName;

    @JsonProperty("idNumber")
    @NotBlank
    private String idNumber;

    @JsonProperty("lastNameKh")
    @NotBlank
    private String lastNameKh;

    @JsonProperty("firstNameKh")
    @NotBlank
    private String firstNameKh;

    @JsonProperty("lastNameEn")
    @NotBlank
    private String lastNameEn;

    @JsonProperty("firstNameEn")
    @NotBlank
    private String firstNameEn;

    @JsonProperty("dob")
    @NotBlank
    private String dob;

    @JsonProperty("gender")
    @NotBlank
    private String gender;

    @JsonProperty("expiredDate")
    @NotBlank
    private String expiredDate;

    @JsonProperty("issuedDate")
    @NotBlank
    private String issuedDate;
}