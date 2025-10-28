package com.internal.feature.open_account.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.service.OpenAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/public/open-account")
@RequiredArgsConstructor
@Slf4j
public class OpenAccountController {

    private final OpenAccountService openAccountService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> openAccount(@Valid @RequestBody CustomerRequest request) {
        log.info("Received account opening request for Legal ID: {}", request.getLegalId());

        CustomerResponse response = openAccountService.openAccount(request);

        log.info("Account opening completed - , CIF: {}", response.getCif());

        return ResponseEntity.ok(ApiResponse.success("Account created successfully!", response));
    }

    @PostMapping("/test-aml")
    public ResponseEntity<ApiResponse<CustomerResponse>> testAmlFull() {
        log.info("Triggering full AML test flow for Legal ID: {}");
        CustomerResponse response = openAccountService.testAmlFlow();
        return ResponseEntity.ok(ApiResponse.success("AML full test flow executed successfully", response));
    }

}