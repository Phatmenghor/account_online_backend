package com.internal.feature.open_account.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    
    @JsonProperty("ErrCode")
    private String errCode;
    
    @JsonProperty("ErrMsg")
    private String errMsg;
    
    @JsonProperty("Content")
    private String content;
    
    @JsonProperty("Status")
    private String status;
    
    @JsonProperty("CIF")
    private String cif;
    
    @JsonProperty("KHRAccount")
    private String khrAccount;
    
    @JsonProperty("USDAccount")
    private String usdAccount;
}