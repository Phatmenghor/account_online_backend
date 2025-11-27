package com.internal.exceptions.error.otp;

public class OtpExpiredException extends OtpException {
    public OtpExpiredException(String phone) {
        super("OTP has expired for phone: " + phone, "OTP_EXPIRED");
    }
}
