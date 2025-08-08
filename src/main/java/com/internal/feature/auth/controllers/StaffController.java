package com.internal.feature.auth.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.auth.dto.request.CardIdRequestDto;
import com.internal.feature.auth.dto.response.StaffResponseDto;
import com.internal.feature.auth.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/v1/admin/staff")
@Validated
@Slf4j
@RequiredArgsConstructor
public class StaffController {

    private final StaffRepository staffRepository;

    // Endpoint to get staff record by card ID
    @PostMapping("/staff-by-cardid")
    public ApiResponse<StaffResponseDto> getStaffByCardId(@RequestBody @Valid CardIdRequestDto request) {
        log.info("Received request to get staff record by card ID: {}", request.getCardId());

        try {
            log.info("Calling staffRepository.getStaffByCardId with cardId: {}", request.getCardId());

            // Call the service method to get staff record by card ID
            StaffResponseDto staff = staffRepository.getStaffByCardId(request.getCardId());

            log.info("Successfully retrieved staff record for card ID: {}", request.getCardId());

            // Return the staff record with success response
            return new ApiResponse<>("success", "Staff response successfully", staff);

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument provided for card ID {}: {}", request.getCardId(), e.getMessage(), e);
            return new ApiResponse<>("error", "Invalid card ID provided: " + e.getMessage(), null);

        } catch (RuntimeException e) {
            log.error("Runtime exception occurred for card ID {}: {}", request.getCardId(), e.getMessage(), e);

            if (e.getMessage() != null && e.getMessage().contains("Staff not found")) {
                log.warn("Staff not found for card ID: {}", request.getCardId());
                return new ApiResponse<>("error", "Staff not found with provided card ID", null);
            }

            return new ApiResponse<>("error", "Runtime error: " + e.getMessage(), null);

        } catch (SQLException e) {
            log.error("Database error occurred while fetching staff record for card ID {}: {}", request.getCardId(), e.getMessage(), e);
            log.error("SQL State: {}, Error Code: {}", e.getSQLState(), e.getErrorCode());
            return new ApiResponse<>("error", "Database error: " + e.getMessage(), null);

        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching staff record for card ID {}: {}", request.getCardId(), e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            e.printStackTrace(); // This will show the full stack trace in logs
            return new ApiResponse<>("error", "Unexpected error: " + e.getMessage(), null);
        }
    }

    // Endpoint to get staff record by id card
    @PostMapping("/{idCard}")
    public ApiResponse<StaffResponseDto> getStaffById(@PathVariable("idCard") String idCard) {
        log.info("Received request to get staff record");

        try {
            // Call the service method to get the staff record
            StaffResponseDto staff = staffRepository.getStaffByCardId(idCard);

            log.debug("Staff details: {}", staff);

            // Return the staff record in ApiResponse with HTTP-200 semantics
            return new ApiResponse<>("success", "Staff id card response successfully", staff);

        } catch (SQLException e) {
            log.error("Database error occurred while fetching staff record: {}", e.getMessage(), e);
            return new ApiResponse<>("error", "Database error occurred while fetching staff record", null);

        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching staff record: {}", e.getMessage(), e);
            return new ApiResponse<>("error", "Internal Server Error", null);
        }
    }
}