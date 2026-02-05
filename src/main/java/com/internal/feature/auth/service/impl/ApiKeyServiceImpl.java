package com.internal.feature.auth.service.impl;

import com.internal.feature.auth.dto.response.ApiKeyResponseDto;
import com.internal.feature.auth.models.ApiKeyEntity;
import com.internal.feature.auth.repository.ApiKeyRepository;
import com.internal.feature.auth.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${aml.api.key}")
    private String defaultApiKey;

    @Value("${aml.api.secret-key}")
    private String defaultSecretKey;

    /**
     * validateKey
     * Checks if the apiKey exists and is active, and if the secretKey matches the hash.
     * Fallback to default YML configuration if not found in DB.
     */
    @Override
    public boolean validateKey(String apiKey, String secretKey) {
        // 1. Check if it matches the Default Static Key (from YML)
        if (defaultApiKey.equals(apiKey)) {
            // Validate Secret for Default Key
            if (defaultSecretKey.equals(secretKey)) {
                return true;
            } else {
                 log.warn("Invalid Secret Key for Default API Key");
                 return false;
            }
        }

        // 2. Check Database for Client Keys
        Optional<ApiKeyEntity> keyEntityOpt = apiKeyRepository.findByApiKey(apiKey);

        if (keyEntityOpt.isEmpty()) {
            log.warn("API Key not found: {}", apiKey);
            return false;
        }

        ApiKeyEntity entity = keyEntityOpt.get();

        if (!entity.isActive()) {
            log.warn("API Key is inactive: {}", apiKey);
            return false;
        }

        if (!passwordEncoder.matches(secretKey, entity.getSecretKeyHash())) {
            log.warn("Invalid Secret Key for API Key: {}", apiKey);
            return false;
        }

        return true;
    }

    /**
     * createApiKey
     * Generates a new API Key (UUID) and Secret Key for a specific client.
     */
    @Override
    public ApiKeyResponseDto createApiKey(String clientName) {
        String apiKey = UUID.randomUUID().toString();
        String secretKey = generateSecretKey();

        ApiKeyEntity entity = new ApiKeyEntity();
        entity.setApiKey(apiKey);
        entity.setSecretKeyHash(passwordEncoder.encode(secretKey));
        entity.setClientName(clientName);
        entity.setActive(true);
        apiKeyRepository.save(entity);

        return ApiKeyResponseDto.builder()
                .clientName(clientName)
                .apiKey(apiKey)
                .secretKey(secretKey)
                .message("Save this Secret Key immediately! It cannot be retrieved later.")
                .build();
    }

    private String generateSecretKey() {
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
}
