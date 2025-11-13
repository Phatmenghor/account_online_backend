package com.internal.feature.logs_report.service;

import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;

public interface AccountOnlineOpenFinalService {

    /**
     * Save AccountOnlineSuccessLog after customer successfully opened account.
     *
     * @param request     the customer request containing user info
     * @param imagePaths  the image paths returned after saving NID & Selfie
     * @return the persisted AccountOnlineSuccessLog entity
     */
    AccountOnlineFinal saveFinalLog(
            CustomerRequest request,
            CustomerResponse accountInfo,
            AmlStatusDto amlProcessResult,
            CustomerImageUploadResponseDto imagePaths
    );
}
