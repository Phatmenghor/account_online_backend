package com.internal.exceptions.error.openaccount;

import lombok.Getter;

@Getter
public class HighRiskCustomerException extends RuntimeException {
    private final String rating;
    
    public HighRiskCustomerException(String rating) {
        super(com.internal.utils.constants.AppConstants.ACCOUNT_RISK);
        this.rating = rating;
    }
}
