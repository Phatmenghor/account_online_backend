package com.account_sell.enumation;

import lombok.Getter;

@Getter
public enum AccountType {
    CASA("Casa"),
    LOAN("Loan"),
    FD_RD("FD / RD"),
    DOB("DOB"),
    PHONE("Phone"),
    NORMAL("Normal"),
    ALL("All");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getPrefix() {
        switch (this) {
            case CASA: return "000";
            case LOAN: return "400";
            case FD_RD: return "800";
            case DOB:
            case PHONE:
            case NORMAL:
            default: return "";
        }
    }
}