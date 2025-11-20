package com.internal.utils.constants;

import java.util.List;
import java.util.stream.Collectors;

public final class ErrorMessage {

    private ErrorMessage() {
        // Prevent instantiation
    }

    public static final String CAMDX_VALIDATE = "CAMDX_VALIDATE";

    public static final String LAST_NAME_EN = "Last Name is required or invalid";
    public static final String FIRST_NAME_EN = "First Name is required or invalid";
    public static final String GENDER = "Gender is required or invalid";
    public static final String DOB = "Date of Birth is required or invalid";
    public static final String MISSING_NID = "User is missing NID";

    /**
     * Generates a single error message for the given failed fields
     * @param errors List of error constants (LAST_NAME_EN, DOB, etc.)
     * @return Combined error message as a single string
     */
    public static String createValidationMessage(List<String> errors) {
        return errors.stream()
                .collect(Collectors.joining("; "));
    }
}
