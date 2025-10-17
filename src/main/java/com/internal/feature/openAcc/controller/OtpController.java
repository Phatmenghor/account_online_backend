package com.internal.feature.openAcc.controller;

import com.internal.feature.openAcc.dto.request.ClsSMS;
import com.internal.feature.openAcc.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Slf4j
public class OtpController {

    private final OtpService otpService;

    @PostMapping
    @Operation(summary = "(Not Ready)")
    public ResponseEntity<Integer> sendSms(@RequestBody ClsSMS data) {
        int result = otpService.processSms(data);
        log.info("SMS processed for phone: {}, result: {}", data.getPhone(), result);
        return ResponseEntity.ok(result);
    }
}
