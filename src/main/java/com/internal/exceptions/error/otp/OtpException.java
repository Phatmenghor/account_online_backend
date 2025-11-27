package com.internal.exceptions.error.otp;

import lombok.Getter;

@Getter
public class OtpException extends RuntimeException {
    private final String errorCode;
    private final Object data;

    public OtpException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
    }

    public OtpException(String message, String errorCode, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }
}
