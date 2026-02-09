package com.internal.feature.auth.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.auth.dto.request.CreateApiKeyRequestDto;
import com.internal.feature.auth.dto.response.ApiKeyResponseDto;
import com.internal.feature.auth.service.ApiKeyService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
@Slf4j
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApiKeyResponseDto>> createApiKey(@Valid @RequestBody CreateApiKeyRequestDto request) {
        log.info("Request to create API key for client: {}", request.getClientName());
        ApiKeyResponseDto response = apiKeyService.createApiKey(request.getClientName());
        log.info("API Key created successfully for client: {}", request.getClientName());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.API_KEY_CREATED, response));
    }
}
