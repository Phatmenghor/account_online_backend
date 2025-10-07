package com.internal.feature.attendance.dto.resposne;

import com.internal.enumation.AttendanceStatus;
import com.internal.enumation.AttendanceType;
import com.internal.enumation.LeaveRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDto {

    private Long id;
    private Long userId;
    private String userIdCard;
    private String userFullName;
    private String userEmail;
    private String userPosition;
    private AttendanceType type;
    private AttendanceStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalDays;
    private LeaveRequest leaveRequest;
    private String reason;
    private String approvedByIdCard;
    private String approvedByFullName;
    private LocalDateTime approvedAt;
    private String approvalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}