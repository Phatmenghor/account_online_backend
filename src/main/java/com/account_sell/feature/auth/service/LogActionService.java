package com.account_sell.feature.auth.service;

import com.account_sell.feature.auth.dto.request.LogActionCreateDTO;
import com.account_sell.feature.auth.dto.response.LogActionDTO;

public interface LogActionService {

    LogActionDTO createLogAction(LogActionCreateDTO createDTO);
}