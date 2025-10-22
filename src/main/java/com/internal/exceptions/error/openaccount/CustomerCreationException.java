package com.internal.exceptions.error.openaccount;

public class CustomerCreationException extends RuntimeException {
    public CustomerCreationException(String message) {
        super(message);
    }
    
    public CustomerCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}