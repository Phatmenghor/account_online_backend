package com.internal.exceptions.error.openaccount;

import lombok.Getter;

@Getter
public class AccountExistsException extends RuntimeException {
    private final String cif;
    
    public AccountExistsException(String cif) {
        super("Account already exists for CIF: " + cif);
        this.cif = cif;
    }
}