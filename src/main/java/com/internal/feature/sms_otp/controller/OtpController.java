package com.internal.feature.sms_otp.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.sms_otp.dto.request.SendOtpRequest;
import com.internal.feature.sms_otp.dto.request.VerifyOtpRequest;
import com.internal.feature.sms_otp.dto.response.SendOtpResponse;
import com.internal.feature.sms_otp.dto.response.VerifyOtpResponse;
import com.internal.feature.sms_otp.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/public/otp")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        
        log.info("API: Send OTP request - Phone: {}", request.getPhone());
        SendOtpResponse response = otpService.sendOtp(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully!", response)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        
        log.info("API: Verify OTP request - Phone: {}", request.getPhone());
        VerifyOtpResponse response = otpService.verifyOtp(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("OTP verified successfully!", response)
        );
    }
}
