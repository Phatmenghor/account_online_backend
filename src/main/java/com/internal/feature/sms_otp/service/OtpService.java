package com.internal.feature.sms_otp.service;

import com.internal.exceptions.error.otp.*;
import com.internal.feature.sms_otp.dto.request.SendOtpRequest;
import com.internal.feature.sms_otp.dto.request.VerifyOtpRequest;
import com.internal.feature.sms_otp.dto.response.SendOtpResponse;
import com.internal.feature.sms_otp.dto.response.VerifyOtpResponse;

public interface OtpService {

    /**
     * Send OTP to phone number
     * Creates a new OTP record in database and sends SMS
     * 
     * @param request SendOtpRequest containing phone number
     * @return SendOtpResponse with OTP details
     * @throws OtpCooldownException if cooldown period not elapsed
     * @throws OtpAttemptsExceededException if max attempts exceeded
     */
    SendOtpResponse sendOtp(SendOtpRequest request);

    /**
     * Verify OTP code
     * Checks if OTP is valid and not expired
     * 
     * @param request VerifyOtpRequest containing phone and OTP code
     * @return VerifyOtpResponse with verification result
     * @throws OtpNotFoundException if no OTP found
     * @throws OtpInvalidException if OTP code is incorrect
     * @throws OtpExpiredException if OTP has expired
     * @throws OtpAttemptsExceededException if max attempts exceeded
     */
    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);
}
