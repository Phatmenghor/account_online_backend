package com.internal.enumation;

public enum AccountOpeningRequestStatusEnum {
    PENDING("Pending Admin Review"),
    APPROVED("Approved by Admin"),
    REJECTED("Rejected by Admin"),
    COMPLETED("Account Opening Completed");

    private final String description;

    AccountOpeningRequestStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
