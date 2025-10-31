package com.internal.feature.aml.dto.response;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.feature.auth.dto.response.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmlStatusDto {
    private Long id;
    private String originalRequest;
    private String originalResponse;
    private CustomerAmlDto customerInfo;
    private AmlStatusEnum status;
    private UserResponseDto approvedBy;
    private UserResponseDto rejectedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
