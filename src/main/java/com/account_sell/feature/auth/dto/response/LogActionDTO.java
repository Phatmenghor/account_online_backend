package com.account_sell.feature.auth.dto.response;

import com.account_sell.enumation.ActionEnum;
import com.account_sell.enumation.StatusLogEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogActionDTO {
    private Long id;
    
    @NotNull(message = "Action type is required")
    private ActionEnum actionType;

    private StatusLogEnum statusLog;
    private String statusCode;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}