package com.internal.feature.open_account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmlResponseDto {
        private String RiskLevel;
        private String ActionTaken;
        private Object[] RulesTriggered;
        private String ServiceName;
        private int TotalRulesScore;
        private String TrxnID;
}