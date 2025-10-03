package com.internal.feature.auth.dto.request;

import com.internal.enumation.RoleEnum;
import com.internal.enumation.UserPermission;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterRequestDto {

    @NotBlank(message = "Id card is required")
    @Size(min = 4, message = "Id card must have at least 4 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least 6 characters")
    private String password;

    private String fullName;

    private UserPermission userPermission;

    private RoleEnum role;
    private String position;
}
