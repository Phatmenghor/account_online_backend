package com.internal.exceptions.error.openaccount;

import com.internal.utils.constants.AppConstants;
import lombok.Getter;

@Getter
public class AccountExistsException extends RuntimeException {
    private final String cif;
    
    public AccountExistsException(String cif) {
        super(AppConstants.MSG_ACCOUNT_EXISTS_ERR);
        this.cif = cif;
    }
}
