package com.internal.exceptions.error.openaccount;

public class T24ServiceException extends RuntimeException {
    public T24ServiceException(String message) {
        super(message);
    }
    
    public T24ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

