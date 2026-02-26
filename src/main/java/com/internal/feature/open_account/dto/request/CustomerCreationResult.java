package com.internal.feature.open_account.dto.request;

import lombok.Data;

@Data
public class CustomerCreationResult {
    private final String cif;
    private final String mnemonic;

    public CustomerCreationResult(String cif, String mnemonic) {
        this.cif = cif;
        this.mnemonic = mnemonic;
    }
}