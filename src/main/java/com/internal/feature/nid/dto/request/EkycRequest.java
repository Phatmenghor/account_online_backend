package com.internal.feature.nid.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EkycRequest {
    @JsonProperty("userInfo")
    private EkycUserInfoRequest userInfo;

    @JsonProperty("faceImg")
    @NotNull
    private String faceImg;

    @JsonProperty("idImage")
    @NotNull
    private String idImage;
}