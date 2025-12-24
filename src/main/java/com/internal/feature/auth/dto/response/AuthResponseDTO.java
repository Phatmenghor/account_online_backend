package com.internal.feature.auth.dto.response;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer ";
    private UserResponseDto userRole;

    private String refreshToken;

    public AuthResponseDTO(String accessToken, String refreshToken, UserResponseDto userRole) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userRole = userRole;
    }

    // You may want to add a method that combines accessToken and tokenType for convenience
    public String getFullToken() {
        return tokenType + accessToken;
    }
}
