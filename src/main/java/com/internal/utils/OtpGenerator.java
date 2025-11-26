package com.internal.utils;

import com.internal.config.CpbProperties;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class OtpGenerator {

    private static final String DIGITS = "0123456789";
    private final SecureRandom random = new SecureRandom();
    private final CpbProperties cpbProperties;
    private final Environment env;

    /**
     * Generate a 6-digit OTP code
     * @return 6-digit OTP string
     */
    public String generate() {
//        String environment = cpbProperties.getEnvironment();
//        if (AppConstants.ENV_DEVELOPMENT.equalsIgnoreCase(environment)) {
//            return AppConstants.DEFAULT_DEV_OTP;
//        }

        // Return default OTP in UAT
        if (Arrays.asList(env.getActiveProfiles()).contains("uat")) {
            return AppConstants.DEFAULT_DEV_OTP; // e.g., "123456"
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
