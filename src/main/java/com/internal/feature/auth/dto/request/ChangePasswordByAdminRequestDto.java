package com.internal.feature.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ChangePasswordByAdminRequestDto {

    @NotNull(message = "id is required")
    private Long id;

    @Size(min = 6, message = "Password must have at least 6 characters")
    @NotBlank(message = "New password is required")
    private String newPassword;

    @Size(min = 6, message = "Password must have at least 6 characters")
    @NotBlank(message = "Confirm new password is required")
    private String confirmNewPassword;
}
