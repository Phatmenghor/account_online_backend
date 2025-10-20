
package com.internal.exceptions.error.otp;

import java.util.HashMap;
import java.util.Map;

public class OtpAttemptsExceededException extends OtpException {
    public OtpAttemptsExceededException(Integer cooldownMinutes) {
        super("Maximum OTP attempts exceeded. Please try again after " + cooldownMinutes + " minutes", 
              "OTP_ATTEMPTS_EXCEEDED", 
              buildData(cooldownMinutes));
    }

    private static Map<String, Object> buildData(Integer cooldownMinutes) {
        Map<String, Object> data = new HashMap<>();
        data.put("lockoutMinutes", cooldownMinutes);
        return data;
    }
}