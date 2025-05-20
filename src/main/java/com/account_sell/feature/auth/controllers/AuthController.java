package com.account_sell.feature.auth.controllers;

import com.account_sell.config.RequiresRole;
import com.account_sell.feature.auth.dto.response.AuthResponseDTO;
import com.account_sell.feature.auth.dto.request.LoginRequestDto;
import com.account_sell.feature.auth.dto.request.RegisterRequestDto;
import com.account_sell.feature.auth.dto.response.UserResponseDto;
import com.account_sell.exceptions.response.ApiResponse;
import com.account_sell.feature.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("login")
    public ApiResponse<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDto loginDto) {
        log.info("Login request received for user: {}", loginDto.getEmail());

        AuthResponseDTO authResponse = authService.login(loginDto);

        log.info("Login successful for user: {}", loginDto.getEmail());

        return new ApiResponse<>(
                "success",
                "Login successful",
                authResponse
        );
    }

    @PostMapping("register")
    public ApiResponse<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto registerDto) {
        log.info("Registration request received for email: {}", registerDto.getEmail());

        UserResponseDto userResponse = authService.register(registerDto);

        log.info("Registration successful for user: {}", registerDto.getEmail());

        return new ApiResponse<>(
                "success",
                "Registration successful",
                userResponse
        );
    }

    @PostMapping("roles")
    public ApiResponse<List<Map<String, Object>>> getAvailableRoles() {
        log.info("Request received to fetch available roles");

        List<Map<String, Object>> roles = authService.getAvailableRoles();

        log.info("Successfully retrieved {} available roles", roles.size());

        return new ApiResponse<>(
                "success",
                "Available roles retrieved successfully",
                roles
        );
    }

    @PostMapping("validate-token")
    public ApiResponse<Boolean> validateToken() {
        log.info("Token validation request received");

        boolean isValid = authService.validateToken();

        if (isValid) {
            log.info("Token validation successful");
            return new ApiResponse<>(
                    "success",
                    "Token is valid",
                    true
            );
        } else {
            log.warn("Token validation failed");
            return new ApiResponse<>(
                    "error",
                    "Token is invalid or user account is inactive",
                    false
            );
        }
    }

    @PostMapping("create-user")
    @RequiresRole(value = {"ADMIN", "DEVELOPER"}, anyRole = true, message = "Only administrators and developers can create users")
    public ApiResponse<UserResponseDto> createUser(@Valid @RequestBody RegisterRequestDto registerDto) {
        log.info("Admin request to create user with email: {}", registerDto.getEmail());

        UserResponseDto userResponse = authService.createUserByAdmin(registerDto);

        log.info("Admin successfully created user with email: {}", registerDto.getEmail());

        return new ApiResponse<>(
                "success",
                "User created successfully",
                userResponse
        );
    }
}