package com.internal.exceptions.error.otp;

import java.util.HashMap;
import java.util.Map;

public class OtpCooldownException extends OtpException {
    public OtpCooldownException(Integer cooldownSeconds) {
        super("Please wait " + cooldownSeconds + " seconds before requesting a new OTP", 
              "OTP_COOLDOWN_ACTIVE",
              buildData(cooldownSeconds));
    }

    private static Map<String, Object> buildData(Integer cooldownSeconds) {
        Map<String, Object> data = new HashMap<>();
        data.put("cooldownSeconds", cooldownSeconds);
        data.put("message", "OTP request too frequent");
        return data;
    }
}