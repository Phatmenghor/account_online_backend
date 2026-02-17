package com.internal.feature.open_account.dto;

import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OpenAccountContext {
    private final CustomerRequest request;
    private Map<String, String> customerInfo;
    private AmlStatusDto amlResult;
    private String cif;
    private String mnemonic;
    private String khrAccount;
    private String usdAccount;
    private String mbActivationCode;
}
