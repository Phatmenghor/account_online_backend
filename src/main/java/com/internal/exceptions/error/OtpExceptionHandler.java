package com.internal.exceptions.error;

import com.internal.exceptions.error.otp.OtpAttemptsExceededException;
import com.internal.exceptions.error.otp.OtpCooldownException;
import com.internal.exceptions.error.otp.OtpInvalidException;
import com.internal.exceptions.error.otp.OtpNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)  // Run BEFORE GlobalExceptionHandler
@Slf4j
public class OtpExceptionHandler {

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<Map<String, Object>> handleOtpInvalidException(OtpInvalidException ex) {
        log.warn("Invalid OTP - Message: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", String.format("Invalid OTP code. You have %d attempt%s remaining.",
                ex.getRemainingAttempts(),
                ex.getRemainingAttempts() == 1 ? "" : "s"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(OtpAttemptsExceededException.class)
    public ResponseEntity<Map<String, Object>> handleOtpAttemptsExceededException(OtpAttemptsExceededException ex) {
        log.warn("OTP attempts exceeded - Message: {}", ex.getMessage());

        // Get remaining seconds from exception
        long remainingSeconds = ex.getLockoutMinutes();
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;

        // User-friendly message with time breakdown
        String timeMessage;
        if (minutes > 0 && seconds > 0) {
            timeMessage = String.format("%d minute%s and %d second%s",
                    minutes, minutes == 1 ? "" : "s",
                    seconds, seconds == 1 ? "" : "s");
        } else if (minutes > 0) {
            timeMessage = String.format("%d minute%s", minutes, minutes == 1 ? "" : "s");
        } else {
            timeMessage = String.format("%d second%s", seconds, seconds == 1 ? "" : "s");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", String.format("Too many failed attempts. Please try again in %s.", timeMessage));

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(OtpCooldownException.class)
    public ResponseEntity<Map<String, Object>> handleOtpCooldownException(OtpCooldownException ex) {
        log.warn("OTP cooldown active - Message: {}", ex.getMessage());

        int remainingSeconds = ex.getRemainingSeconds();

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", String.format("Please wait %d second%s before requesting a new OTP.",
                remainingSeconds, remainingSeconds == 1 ? "" : "s"));

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(OtpNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOtpNotFoundException(OtpNotFoundException ex) {
        log.warn("OTP not found - Message: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("message", "No active OTP found. Please request a new OTP.");

        Map<String, Object> details = new HashMap<>();
        details.put("userMessage", "No verification code was found for this phone number. Please request a new one.");
        response.put("details", details);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
