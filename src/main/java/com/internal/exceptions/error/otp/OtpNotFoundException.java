package com.internal.exceptions.error.otp;

public class OtpNotFoundException extends RuntimeException {
    public OtpNotFoundException(String phone) {
        super("No active OTP found for phone: " + phone);
    }
}
