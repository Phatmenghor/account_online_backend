package com.internal.feature.logs_report.service;

import com.internal.feature.logs_report.model.AccountOnlineSuccessLog;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;

public interface AccountOnlineOpenFinalLogsService {

    /**
     * Save AccountOnlineSuccessLog after customer successfully opened account.
     *
     * @param request     the customer request containing user info
     * @param imagePaths  the image paths returned after saving NID & Selfie
     * @return the persisted AccountOnlineSuccessLog entity
     */
    AccountOnlineSuccessLog saveFinalLog(CustomerRequest request, CustomerImageUploadResponseDto imagePaths);
}
