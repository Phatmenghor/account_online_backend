package com.internal.feature.nid.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EkycFaceRequest {

    @JsonProperty("idImage")
    @javax.validation.constraints.NotNull
    private String idImage;

    private String applicationName = "DEVELOPMENT";
}
