package com.internal.utils;

import com.internal.config.CpbProperties;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class OtpGenerator {

    private static final String DIGITS = "0123456789";
    private final SecureRandom random = new SecureRandom();
    private final CpbProperties cpbProperties;

    public String generate() {
        String environment = cpbProperties.getEnvironment();
        if ("development".equalsIgnoreCase(environment) || "uat".equalsIgnoreCase(environment) || "local".equalsIgnoreCase(environment)) {
            return AppConstants.DEFAULT_DEV_OTP;
        }
        int otpLength = cpbProperties.getOtp().getLength() > 0
                ? cpbProperties.getOtp().getLength()
                : AppConstants.DEFAULT_OTP_LENGTH;
        StringBuilder otp = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            otp.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }
}