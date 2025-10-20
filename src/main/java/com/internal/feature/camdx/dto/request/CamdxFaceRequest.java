package com.internal.feature.camdx.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CamdxFaceRequest {

    @JsonProperty("idImage")
    private String idImage;
    private String applicationName;
}
