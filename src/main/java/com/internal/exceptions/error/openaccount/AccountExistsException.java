package com.internal.exceptions.error.openaccount;

import lombok.Getter;

@Getter
public class AccountExistsException extends RuntimeException {
    private final String cif;
    
    public AccountExistsException(String cif) {
        super(com.internal.utils.constants.AppConstants.ACCOUNT_ALREADY_EXIST);
        this.cif = cif;
    }
}
