package com.internal.feature.aml.dto.response;

import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.enumation.AmlStatusEnum;
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

    private CustomerAmlDto customerInfo; // Nested DTO for customer details

    private UserResponseDto changedBy;   // Approved or rejected by

    private AmlStatusEnum status;        // AML status

    private String screeningResult;      // AML screening JSON result

    private String RiskLevel;
    private String ActionTaken;
    private Object[] RulesTriggered;
    private String ServiceName;
    private int TotalRulesScore;
    private String TrxnID;

    private String remarks;              // Admin remarks
}
