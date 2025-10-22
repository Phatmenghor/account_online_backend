package com.internal.exceptions.error.openaccount;

import lombok.Getter;

@Getter
public class HighRiskCustomerException extends RuntimeException {
    private final String rating;
    
    public HighRiskCustomerException(String rating) {
        super("High-risk customer with rating: " + rating);
        this.rating = rating;
    }
}