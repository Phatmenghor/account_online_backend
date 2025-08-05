package com.account_sell.feature.auth.dto.request;

import com.account_sell.enumation.CamdxLogEnum;
import lombok.Data;

@Data
public class RequestLogReport {
    private String requestDate;
    private String requestType;
    private CamdxLogEnum status;
    private int requestCount;
}
