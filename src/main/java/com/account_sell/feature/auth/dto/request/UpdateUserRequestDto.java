package com.account_sell.feature.auth.dto.request;

import com.account_sell.enumation.StatusData;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    private String idCard;
    private String email;
    private StatusData status;
    private String profileUrl;
}
