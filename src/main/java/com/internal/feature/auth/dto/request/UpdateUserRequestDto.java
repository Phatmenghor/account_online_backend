package com.internal.feature.auth.dto.request;

import com.internal.enumation.StatusData;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    private String username;
    private String email;
    private String fullName;
    private StatusData status;
    private String profileUrl;
    private String position;
}
