package com.internal.feature.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChangePasswordRequestDto {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Size(min = 6, message = "Password must have at least 6 characters")
    @NotBlank(message = "New password is required")
    private String newPassword;

    @Size(min = 6, message = "Password must have at least 6 characters")
    @NotBlank(message = "Confirm new password is required")
    private String confirmNewPassword;
}
