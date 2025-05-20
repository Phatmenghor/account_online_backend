package com.account_sell.feature.auth.dto.request;

import com.account_sell.enumation.RoleEnum;
import com.account_sell.enumation.StatusData;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RegisterRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least 6 characters")
    private String password;

    private String profileUrl;

    @NotNull(message = "Role is required")
    private RoleEnum role;

    @NotNull(message = "Status is required")
    private StatusData status;
}
