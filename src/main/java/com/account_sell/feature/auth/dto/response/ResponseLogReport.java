package com.account_sell.feature.auth.dto.response;

import com.account_sell.enumation.CamdxLogEnum;
import lombok.Data;

@Data
public class ResponseLogReport {
    private String requestDate;
    private String requestType;
    private CamdxLogEnum status;
    private int requestCount;
}
