package com.internal.exceptions.error.otp;

import lombok.Getter;

@Getter
public class OtpInvalidException extends RuntimeException {
    private final int remainingAttempts;

    public OtpInvalidException(int remainingAttempts) {
        super("Invalid OTP code");
        this.remainingAttempts = remainingAttempts;
    }
}
