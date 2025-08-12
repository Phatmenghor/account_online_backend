package com.internal.feature.auth.dto.request;

import com.internal.enumation.StatusData;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    private String idCard;
    private String email;
    private String fullName;
    private StatusData status;
    private String profileUrl;
    private String position;
    private String branch;
}
