package com.internal.feature.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiKeyResponseDto {
    private String clientName;
    private String apiKey;
    private String secretKey;
    private String message;
}
