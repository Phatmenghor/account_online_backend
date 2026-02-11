package com.internal.exceptions.error.openaccount;

import com.internal.utils.constants.AppConstants;
import lombok.Getter;

@Getter
public class HighRiskCustomerException extends RuntimeException {
    private final String rating;
    
    public HighRiskCustomerException(String rating) {
        super(AppConstants.AML_NEED_REVIEW_MSG);
        this.rating = rating;
    }
}
