package com.internal.feature.nid.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EkycUserInfoRequest {
    @JsonProperty("idNumber") @NotNull
    private String idNumber;
    @JsonProperty("firstNameKh") @NotNull private String firstNameKh;
    @JsonProperty("lastNameKh") @NotNull private String lastNameKh;
    @JsonProperty("firstNameEn") @NotNull private String firstNameEn;
    @JsonProperty("lastNameEn") @NotNull private String lastNameEn;
    @JsonProperty("gender") @NotNull private String gender;
    @JsonProperty("issuedDate") @NotNull private String issuedDate;
    @JsonProperty("dob") @NotNull private String dob;
    @JsonProperty("expiredDate") @NotNull private String expiredDate;
}