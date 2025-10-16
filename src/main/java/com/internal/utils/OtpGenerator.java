package com.internal.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpGenerator {

    private static final String DIGITS = "0123456789";

    public String generate(int length) {
        StringBuilder otp = new StringBuilder();
        Random rnd = new Random();
        while (otp.length() < length) {
            char c = DIGITS.charAt(rnd.nextInt(DIGITS.length()));
            if (otp.indexOf(String.valueOf(c)) == -1) otp.append(c);
        }
        return otp.toString();
    }
}