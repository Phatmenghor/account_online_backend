package com.internal.exceptions.error.otp;

public class OtpNotFoundException extends OtpException {
    public OtpNotFoundException(String phone) {
        super("No valid OTP found for phone: " + phone, "OTP_NOT_FOUND");
    }
}
