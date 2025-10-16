package com.internal.feature.openAcc.controller;

import com.internal.feature.openAcc.dto.request.ClsSMS;
import com.internal.feature.openAcc.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Slf4j
public class OtpController {

    private final OtpService otpService;

    @PostMapping
    public ResponseEntity<Integer> sendSms(@RequestBody ClsSMS data) {
        int result = otpService.processSms(data);
        log.info("SMS processed for phone: {}, result: {}", data.getPhone(), result);
        return ResponseEntity.ok(result);
    }
}
