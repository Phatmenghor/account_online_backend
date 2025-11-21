package com.internal.feature.logs_report.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.logs_report.dto.request.AccountOnlineFinalLogRequestDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.service.serviceImpl.AccountOnlineOpenFinalServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account-online-final")
@RequiredArgsConstructor
@Slf4j
public class AccountOnlineFinalController {

    private final AccountOnlineOpenFinalServiceImpl openFinalService;

    @PostMapping()
    public ResponseEntity<ApiResponse<AccountOnlineFinalResponseDto>> getAccountByCifOrLegalId(@RequestBody AccountOnlineFinalLogRequestDto request) {
        log.info("Fetching Account By CIF : {} , Legal Id : {} ", request.getCif(),request.getLegalId());
        AccountOnlineFinalResponseDto dto = openFinalService.findAccountByCifOrLegalId(request);
        log.info("Successfully get Account By CIF : {} , Legal Id : {}",request.getCif(),request.getLegalId());
        return ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", dto));

    }

}
