package com.account_sell.feature.auth.dto.request;

import com.account_sell.enumation.ActionEnum;
import com.account_sell.enumation.StatusLogEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogActionCreateDTO {
    @NotNull(message = "Action type is required")
    private ActionEnum actionType;

    private StatusLogEnum statusLog;
    private String statusCode;

    private Long userId;
}