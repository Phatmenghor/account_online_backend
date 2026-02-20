package com.internal.feature.aml.dto.response;

import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.enumation.AmlStatusEnum;
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
public class AmlHistoryDto {

    private Long id;

    private CustomerAmlDto customerInfo;

    private AmlStatusEnum status;
    private String screeningResult;

    private String riskLevel;
    private String actionTaken;
    private String rulesTriggered;
    private String serviceName;
    private int totalRulesScore;
    private String trxnID;

    private UserResponseDto approvedBy;
    private UserResponseDto rejectedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String currentAddressName;
    private String currentAddressCode;
    private String placeOfBirthName;
    private String placeOfBirthCode;
    private String maritalStatus;
    private String occupationCode;
    private String occupationStatus;

    private String remarks;

    private String nidImageName;
    private String selfieImageName;
}
