package com.internal.feature.attendance.controller;

import com.internal.config.RequiresRole;
import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.attendance.dto.request.ApprovalRequestDto;
import com.internal.feature.attendance.dto.request.AttendanceRequestDto;
import com.internal.feature.attendance.dto.request.AttendanceUpdateRequestDto;
import com.internal.feature.attendance.dto.request.GetAllAttendanceRequestDto;
import com.internal.feature.attendance.dto.resposne.AllAttendanceResponseDto;
import com.internal.feature.attendance.dto.resposne.AttendanceResponseDto;
import com.internal.feature.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance Management")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> createAttendanceRequest(
            @Valid @RequestBody AttendanceRequestDto requestDto) {
        log.info("Received create attendance request for type: {}", requestDto.getType());

        AttendanceResponseDto response = attendanceService.createAttendanceRequest(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Attendance request created successfully", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllAttendanceResponseDto>> getAllAttendances(
            @RequestBody GetAllAttendanceRequestDto requestDto) {
        log.info("Received get all attendances request");

        AllAttendanceResponseDto response = attendanceService.getAllAttendances(requestDto);

        return ResponseEntity.ok(new ApiResponse<>("success",
                String.format("Retrieved %d attendances", response.getContent().size()), response));
    }

    @PostMapping("/all-list")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getAllListAttendances(
            @RequestBody GetAllAttendanceRequestDto requestDto) {
        log.info("Received get all attendances request");

        List<AttendanceResponseDto> response = attendanceService.getAllListAttendances(requestDto);

        return ResponseEntity.ok(new ApiResponse<>("success", "Retrieved all attendances", response));
    }

    @PostMapping("/my")
    public ResponseEntity<ApiResponse<AllAttendanceResponseDto>> getMyAttendances(
            @RequestBody GetAllAttendanceRequestDto requestDto) {
        log.info("Received get my attendances request");

        AllAttendanceResponseDto response = attendanceService.getMyAttendances(requestDto);

        return ResponseEntity.ok(new ApiResponse<>("success",
                String.format("Retrieved %d of your attendances", response.getContent().size()), response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> getAttendanceById(@PathVariable Long id) {
        log.info("Received get attendance by ID request: {}", id);

        AttendanceResponseDto response = attendanceService.getAttendanceById(id);

        return ResponseEntity.ok(new ApiResponse<>("success", "Attendance retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> updateAttendance(
            @PathVariable Long id,
            @RequestBody AttendanceUpdateRequestDto requestDto) {
        log.info("Received update attendance request for ID: {}", id);

        AttendanceResponseDto response = attendanceService.updateAttendance(id, requestDto);

        return ResponseEntity.ok(new ApiResponse<>("success", "Attendance updated successfully", response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> cancelAttendance(@PathVariable Long id) {
        log.info("Received cancel attendance request for ID: {}", id);

        AttendanceResponseDto response = attendanceService.cancelAttendance(id);

        return ResponseEntity.ok(new ApiResponse<>("success", "Attendance cancelled successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAttendance(@PathVariable Long id) {
        log.info("Received delete attendance request for ID: {}", id);

        attendanceService.deleteAttendance(id);

        return ResponseEntity.ok(new ApiResponse<>("success", "Attendance deleted successfully",
                "Attendance with ID " + id + " has been deleted"));
    }

    @PostMapping("/{id}/approve")
    @RequiresRole(value = {"SUPER"})
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> approveOrRejectAttendance(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDto approvalDto) {
        log.info("Received approval request for attendance ID: {} with status: {}", id, approvalDto.getStatus());

        AttendanceResponseDto response = attendanceService.approveOrRejectAttendance(id, approvalDto);

        String message = approvalDto.getStatus() == com.internal.enumation.AttendanceStatus.APPROVED
                ? "Attendance approved successfully"
                : "Attendance rejected successfully";

        return ResponseEntity.ok(new ApiResponse<>("success", message, response));
    }
}
