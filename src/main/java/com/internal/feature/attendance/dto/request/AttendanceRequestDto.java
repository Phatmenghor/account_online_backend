package com.internal.feature.attendance.dto.request;

import com.internal.enumation.AttendanceType;
import com.internal.enumation.LeaveRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequestDto {

    @NotNull(message = "Attendance type is required")
    private AttendanceType type;

    @NotNull(message = "leaveRequest type is required")
    private LeaveRequest leaveRequest;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    private String reason;
}