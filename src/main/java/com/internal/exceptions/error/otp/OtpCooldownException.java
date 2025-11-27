package com.internal.exceptions.error.otp;

import lombok.Getter;

@Getter
public class OtpCooldownException extends RuntimeException {
    private final int remainingSeconds;

    public OtpCooldownException(int remainingSeconds) {
        super("OTP cooldown period active");
        this.remainingSeconds = remainingSeconds;
    }
}
