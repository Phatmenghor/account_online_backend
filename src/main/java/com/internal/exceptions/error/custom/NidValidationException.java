package com.internal.exceptions.error.custom;

public class NidValidationException extends RuntimeException {
    private final int statusCode;

    public NidValidationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
