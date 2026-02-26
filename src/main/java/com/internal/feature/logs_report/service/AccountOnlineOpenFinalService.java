package com.internal.feature.logs_report.service;

import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.logs_report.dto.request.AccountOnlineFinalLogRequestDto;
import com.internal.feature.logs_report.dto.request.AllAccountOnlineSuccessExcelRequestDto;
import com.internal.feature.logs_report.dto.request.AllAccountOnlineSuccessRequestDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.dto.response.AllAccountOnlineFinalExcelResponseDto;
import com.internal.feature.logs_report.dto.response.AllAccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import org.springframework.transaction.annotation.Transactional;


public interface AccountOnlineOpenFinalService {

    /**
     * Save AccountOnlineFinal after customer successfully opened account.
     *
     * @param request    the customer request containing user info
     * @param imagePaths the image paths returned after saving NID & Selfie
     * @return the persisted AccountOnlineFinal entity
     */
    AccountOnlineFinal saveFinalLog(
            CustomerRequest request,
            CustomerResponse accountInfo,
            AmlStatusDto amlProcessResult,
            CustomerImageUploadResponseDto imagePaths,
            String mbActivationCode
    );

    AllAccountOnlineFinalResponseDto getSuccessOpenAccount(AllAccountOnlineSuccessRequestDto request);

    // Excel
    AllAccountOnlineFinalExcelResponseDto getSuccessOpenAccountExcel(AllAccountOnlineSuccessExcelRequestDto request);

    @Transactional
    void updateFinalLogWithAml(AmlStatusDto amlStatus);

    AccountOnlineFinalResponseDto findAccountByCifOrLegalId(AccountOnlineFinalLogRequestDto requestDto);

}
