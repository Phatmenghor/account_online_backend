package com.account_sell.feature.auth.controllers;

import com.account_sell.exceptions.response.ApiResponse;
import com.account_sell.feature.auth.dto.request.LogActionCreateDTO;
import com.account_sell.feature.auth.dto.response.LogActionDTO;
import com.account_sell.feature.auth.service.LogActionService;
import com.account_sell.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/log-actions")
@RequiredArgsConstructor
@Slf4j
public class LogActionController {

    private final LogActionService logActionService;

    @PostMapping
    public ApiResponse<LogActionDTO> createLogAction(@Valid @RequestBody LogActionCreateDTO createDTO) {
        log.info("Request to create log action: {}", createDTO);
        LogActionDTO createdLogAction = logActionService.createLogAction(createDTO);
        return new ApiResponse<>("success", "log has been create", createdLogAction);
    }
}