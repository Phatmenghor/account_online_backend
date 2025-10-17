package com.internal.feature.nid.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EkycFaceRequest {

    @JsonProperty("idImage")
    @javax.validation.constraints.NotNull
    private String idImage;

    @javax.validation.constraints.NotNull
    @Schema(defaultValue = "DEVELOPMENT", description = "Application name (default is DEVELOPMENT)")
    private String applicationName;
}
