package com.internal.feature.open_account.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmlExternalResponseDto {

    @JsonProperty("RiskLevel")
    private String riskLevel;

    @JsonProperty("ActionTaken")
    private String actionTaken;

    @JsonProperty("RulesTriggered")
    private String rulesTriggered;

    @JsonProperty("ServiceName")
    private String serviceName;

    @JsonProperty("TotalRulesScore")
    private int totalRulesScore;

    @JsonProperty("TrxnID")
    private String trxnID;
}
