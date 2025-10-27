package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.dto.response.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAmlRequestDto {
    private String originalRequest;
    private String originalResponse;
    private AmlStatusEnum status;
    private UserResponseDto approvedBy;
    private UserResponseDto rejectedBy;
}

