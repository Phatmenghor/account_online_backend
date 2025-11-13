package com.internal.feature.logs_report.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.logs_report.dto.request.AccountOnlineFinalLogRequestDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalLogResponseDto;
import com.internal.feature.logs_report.service.serviceImpl.AccountOnlineOpenFinalServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account-online-final-log")
@RequiredArgsConstructor
@Slf4j
public class AccountOnlineFinalLogController {
    private AccountOnlineOpenFinalServiceImpl openFinalService;
    @PostMapping("/get-account-by-cif-legal")
    public ResponseEntity<ApiResponse<AccountOnlineFinalLogResponseDto>> getAccountByCifOrLegalId(@RequestBody AccountOnlineFinalLogRequestDto request) {
        log.info("Fetching Account By CIF : {} , Legal Id : {} ", request.getCif(),request.getLegalId());
        AccountOnlineFinalLogResponseDto dto = openFinalService.findAccountByCifOrLegalId(request);
        log.info("Successfully get Account By CIF : {} , Legal Id : {}",request.getCif(),request.getLegalId());
        return ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", dto));

    }
}
