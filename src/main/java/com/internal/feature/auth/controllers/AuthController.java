package com.internal.feature.auth.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.auth.dto.request.LoginRequestDto;
import com.internal.feature.auth.dto.request.UpdateUserRequestDto;
import com.internal.feature.auth.dto.response.AuthResponseDTO;
import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.service.AuthService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDto loginDto) {
        log.info("Authentication attempt for user: {}", loginDto.getUsername());
        AuthResponseDTO authResponse = authService.login(loginDto);
        log.info("Authentication successful for user: {}", loginDto.getUsername());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.LOGIN_SUCCESS, authResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<com.internal.feature.auth.dto.response.TokenRefreshResponseDto>> refreshToken(@Valid @RequestBody com.internal.feature.auth.dto.request.TokenRefreshRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.TOKEN_REFRESHED, authService.refreshToken(requestDto)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        authService.logout(username); 
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.LOGOUT_SUCCESS, null));
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableRoles() {
        log.debug("Fetching available roles");
        List<Map<String, Object>> roles = authService.getAvailableRoles();
        log.debug("Retrieved {} available roles", roles.size());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.ROLES_RETRIEVED, roles));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken() {
        log.debug("Token validation request");
        boolean isValid = authService.validateToken();
        return ResponseEntity.ok(isValid
                ? ApiResponse.success(ResponseMessage.TOKEN_VALID, true)
                : ApiResponse.error(ResponseMessage.TOKEN_INVALID, false));
    }

    @PostMapping("/token/update-profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserProfile(@Valid @RequestBody UpdateUserRequestDto registerDto) {
        log.info("Admin update profile request for ID card: {}", registerDto.getUsername());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserResponseDto userResponse = authService.updateUserProfile(registerDto, authentication.getName());
        log.info("Admin update profile successful for: {}", registerDto.getUsername());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PROFILE_UPDATED, userResponse));
    }
}
