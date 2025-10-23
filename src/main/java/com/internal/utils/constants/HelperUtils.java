package com.internal.utils.constants;

import org.springframework.stereotype.Component;

@Component
public class HelperUtils {
    public static String formatCodeWithLeadingZero(String code, int length) {
        if (code == null || code.isEmpty()) return code;
        try {
            int numeric = Integer.parseInt(code);
            return String.format("%0" + length + "d", numeric);
        } catch (NumberFormatException e) {
            // If code is not numeric, just return it as-is
            return code;
        }
    }
}
