package com.internal.feature.open_account.service;

import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;

public interface OpenAccountService {
    CustomerResponse openAccount(CustomerRequest request) throws Exception;
}
