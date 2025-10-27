package com.internal.exceptions.error.otp;

import lombok.Getter;

@Getter
public class OtpAttemptsExceededException extends RuntimeException {
    private final Long lockoutMinutes; // Actually stores seconds for backward compatibility

    public OtpAttemptsExceededException(Long lockoutSeconds) {
        super("Maximum OTP verification attempts exceeded");
        this.lockoutMinutes = lockoutSeconds; // Field name kept for compatibility
    }
}
