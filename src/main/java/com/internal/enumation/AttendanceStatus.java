package com.internal.enumation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AttendanceStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED;

    @JsonCreator
    public static AttendanceStatus fromString(String value) {
        return AttendanceStatus.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
