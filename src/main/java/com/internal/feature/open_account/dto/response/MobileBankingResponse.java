package com.internal.feature.open_account.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileBankingResponse {

    @JsonProperty("ErrorCode")
    private String code;

    @JsonProperty("ErrorMsg")
    private String message;

    @JsonProperty("Content")
    private String content;
}
