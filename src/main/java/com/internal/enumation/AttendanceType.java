package com.internal.enumation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AttendanceType {
    SICK_LEAVE,
    ANNUAL_LEAVE,
    UNPAID_LEAVE,
    MATERNITY_LEAVE,
    PATERNITY_LEAVE,
    BUSINESS_TRIP,
    REMOTE_WORK,
    PERMISSION,
    OTHER;

    @JsonCreator
    public static AttendanceType fromString(String value) {
        return AttendanceType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
