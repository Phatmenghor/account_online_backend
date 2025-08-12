package com.internal.feature.auth.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.auth.dto.request.CardIdRequestDto;
import com.internal.feature.auth.dto.response.StaffResponseDto;
import com.internal.feature.auth.repository.StaffRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/v1/staff")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Staff Management")
public class StaffController {

    private final StaffRepository staffRepository;
//
//    @PostMapping("/staff-by-cardid")
//    public ResponseEntity<ApiResponse<StaffResponseDto>> getStaffByCardId(@RequestBody @Valid CardIdRequestDto request) throws SQLException {
//        log.info("Fetching staff record by card ID: {}", request.getCardId());
//        StaffResponseDto staff = staffRepository.getStaffByCardId(request.getCardId());
//        log.info("Successfully retrieved staff record for card ID: {}", request.getCardId());
//
//        return ResponseEntity.ok(new ApiResponse<>(
//            "success",
//            "Staff response successfully",
//            staff
//        ));
//    }

    @PostMapping("/{idCard}")
    public ResponseEntity<ApiResponse<StaffResponseDto>> getStaffById(@PathVariable("idCard") String idCard) throws SQLException {
        log.info("Fetching staff record for ID card: {}", idCard);

        StaffResponseDto staff = staffRepository.getStaffByCardId(idCard);
        log.info("Successfully retrieved staff record for ID card: {}", idCard);

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Staff id card response successfully",
            staff
        ));
    }
}