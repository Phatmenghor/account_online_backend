package com.internal.exceptions.error;

import com.internal.exceptions.error.openaccount.*;
import com.internal.exceptions.response.ErrorResponse;
import com.internal.utils.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpenAccountExceptionHandler {

    @ExceptionHandler(DatabaseConnectionException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConnectionException(DatabaseConnectionException ex) {
        log.error("Database connection error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .message(AppConstants.SYSTEM_ERROR)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(HighRiskCustomerException.class)
    public ResponseEntity<ErrorResponse> handleHighRiskCustomerException(HighRiskCustomerException ex) {
        log.warn("High-risk customer detected: Rating {}", ex.getRating());

        Map<String, Object> details = new HashMap<>();
        details.put("rating", ex.getRating());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .message(AppConstants.ACCOUNT_RISK)
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccountExistsException.class)
    public ResponseEntity<ErrorResponse> handleAccountExistsException(AccountExistsException ex) {
        log.warn("Account already exists for CIF: {}", ex.getCif());

        Map<String, Object> details = new HashMap<>();
        details.put("cif", ex.getCif());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .message(AppConstants.ACCOUNT_ALREADY_EXIST)
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccountCreationException.class)
    public ResponseEntity<ErrorResponse> handleAccountCreationException(AccountCreationException ex) {
        log.error("Account creation failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())         // <-- FIX
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(T24ServiceException.class)
    public ResponseEntity<ErrorResponse> handleT24ServiceException(T24ServiceException ex) {
        log.error("T24 service error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .message(AppConstants.SYSTEM_ERROR)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        log.warn("Validation errors: {}", message);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .details(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
