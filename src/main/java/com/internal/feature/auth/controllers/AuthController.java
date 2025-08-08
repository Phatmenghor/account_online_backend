package com.internal.feature.auth.controllers;

import com.internal.config.RequiresRole;
import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.auth.dto.request.LoginRequestDto;
import com.internal.feature.auth.dto.request.RegisterRequestDto;
import com.internal.feature.auth.dto.response.AuthResponseDTO;
import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDto loginDto) {
        log.info("Authentication attempt for user: {}", loginDto.getIdCard());
        
        AuthResponseDTO authResponse = authService.login(loginDto);
        log.info("Authentication successful for user: {}", loginDto.getIdCard());
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Login successful",
            authResponse
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequestDto registerDto) {
        log.info("Registration request for ID card: {}", registerDto.getIdCard());
        
        UserResponseDto userResponse = authService.register(registerDto);
        log.info("Registration successful for user: {}", registerDto.getIdCard());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(
                "success",
                "Registration successful. Account pending approval.",
                userResponse
            ));
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableRoles() {
        log.debug("Fetching available roles");
        
        List<Map<String, Object>> roles = authService.getAvailableRoles();
        log.debug("Retrieved {} available roles", roles.size());
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Available roles retrieved successfully",
            roles
        ));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken() {
        log.debug("Token validation request");
        
        boolean isValid = authService.validateToken();
        String message = isValid ? "Token is valid" : "Token is invalid or user account is inactive";
        String status = isValid ? "success" : "error";
        
        return ResponseEntity.ok(new ApiResponse<>(status, message, isValid));
    }

    @PostMapping("/create-user")
    @RequiresRole(value = {"ADMIN", "SUPER"}, anyRole = true)
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody RegisterRequestDto registerDto) {
        log.info("Admin user creation request for ID card: {}", registerDto.getIdCard());
        
        UserResponseDto userResponse = authService.createUserByAdmin(registerDto);
        log.info("Admin user creation successful for: {}", registerDto.getIdCard());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(
                "success",
                "User created successfully",
                userResponse
            ));
    }
}