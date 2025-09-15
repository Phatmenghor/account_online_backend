package com.internal.enumation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusData {
    ACTIVE,
    DELETE;

    @JsonCreator
    public static StatusData fromString(String value) {
        return StatusData.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
