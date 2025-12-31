package com.internal.feature.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateApiKeyRequestDto {

    @NotBlank(message = "Client name is required")
    private String clientName;
}
