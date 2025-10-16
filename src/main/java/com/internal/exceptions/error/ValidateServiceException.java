package com.internal.exceptions.error;

public class ValidateServiceException extends RuntimeException {
    public ValidateServiceException(String message) {
        super(message);
    }
    public ValidateServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
