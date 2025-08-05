package com.account_sell.feature.auth.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ResponseLogReportExport {
    private List<ResponseLogReport> content;
    private int totalRequestCount;
}
