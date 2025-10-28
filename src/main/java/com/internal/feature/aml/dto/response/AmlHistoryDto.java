package com.internal.feature.aml.dto.response;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.feature.auth.dto.response.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmlHistoryDto {

    private Long id;

    private String originalRequest;

    private String originalResponse;

    private CustomerAmlDto customerInfo;

    private AmlStatusEnum oldStatus;

    private AmlStatusEnum newStatus;

    private UserResponseDto changedBy;
}
