package com.internal.exceptions.error;

import com.internal.exceptions.error.custom.*;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.exceptions.response.ErrorResponse;
import com.internal.utils.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.security.authentication.BadCredentialsException;

import javax.validation.ConstraintViolationException;
import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE) // Run after OtpExceptionHandler
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NidValidationException.class)
    public ResponseEntity<ErrorResponse> handleNidValidationException(NidValidationException ex) {
        log.error("NID Validation failed - Status: {}, Message: {}", ex.getStatusCode(), ex.getMessage());

        // Return user-friendly message with BAD_REQUEST status to frontend
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MasterDataServiceException.class)
    public ResponseEntity<ErrorResponse> handleMasterDataException(MasterDataServiceException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccountCreationException.class)
    public ResponseEntity<ErrorResponse> handleAccountCreationException(AccountCreationException ex) {
        log.warn("Account creation failed: {}", ex.getMessage());

        // Use buildErrorResponse like DuplicateNameException
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ValidateServiceException.class)
    public ResponseEntity<ErrorResponse> handleValidateServiceException(ValidateServiceException ex) {
        log.error("ValidateServiceException: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNameException(DuplicateNameException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
        log.warn("Invalid input: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError) error).getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation errors: {}", errorMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation error: " + errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", errorMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation error: " + errorMessage);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
        log.error("Database error - Code: {}, State: {}, Message: {}",
                ex.getErrorCode(), ex.getSQLState(), ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = ex.getMessage().contains("unique") || ex.getMessage().contains("duplicate")
                ? "Resource already exists"
                : "Data integrity violation";

        return buildErrorResponse(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler({ ResourceAccessException.class, SocketTimeoutException.class, TimeoutException.class,
            EOFException.class })
    public ResponseEntity<ErrorResponse> handleTimeoutException(Exception ex) {
        log.warn("Timeout/Connection error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.REQUEST_TIMEOUT, AppConstants.MSG_CONNECTION_TIMEOUT);
    }

    // FIXED: Single Exception handler that excludes OTP exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof AccountCreationException) {
                String msg = cause.getMessage();
                log.error("Account creation exception propagated: {}", msg, ex);
                return buildErrorResponse(HttpStatus.BAD_REQUEST, msg);
            }
            cause = cause.getCause();
        }

        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, AppConstants.MSG_GENERIC_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}
