package com.internal.feature.auth.service;

import com.internal.feature.auth.dto.response.ApiKeyResponseDto;

public interface ApiKeyService {
    boolean validateKey(String apiKey, String secretKey);
    ApiKeyResponseDto createApiKey(String clientName);
}
