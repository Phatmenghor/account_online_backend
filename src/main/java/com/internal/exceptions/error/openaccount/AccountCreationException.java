package com.internal.exceptions.error.openaccount;

import lombok.Getter;

@Getter
public class AccountCreationException extends RuntimeException {
    private final Object details;

    public AccountCreationException(String message) {
        super(message);
        this.details = null;
    }

    public AccountCreationException(String message, Object details) {
        super(message);
        this.details = details;
    }
}
