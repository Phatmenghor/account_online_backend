package com.internal.feature.open_account.service;

import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import org.springframework.transaction.annotation.Transactional;

public interface OpenAccountService {
    CustomerResponse openAccount(CustomerRequest request);
    CustomerResponse openAccountMock(CustomerRequest request);

    @Transactional
    CustomerResponse testAmlFlow();
}
