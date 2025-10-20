package com.internal.exceptions.error.otp;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OtpInvalidException extends OtpException {
    public OtpInvalidException(Integer remainingAttempts) {
        super("Invalid OTP code", "OTP_INVALID", buildData(remainingAttempts));
    }

    private static Map<String, Object> buildData(Integer remainingAttempts) {
        Map<String, Object> data = new HashMap<>();
        data.put("remainingAttempts", remainingAttempts);
        return data;
    }
}