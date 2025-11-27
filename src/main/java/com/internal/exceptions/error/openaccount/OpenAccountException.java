package com.internal.exceptions.error.openaccount;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OpenAccountException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> data;
    
    public OpenAccountException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = new HashMap<>();
    }
    
    public OpenAccountException(String errorCode, String message, Map<String, Object> data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }
}
