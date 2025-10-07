package com.internal.feature.attendance.dto.request;

import com.internal.enumation.AttendanceStatus;
import com.internal.enumation.AttendanceType;
import com.internal.enumation.LeaveRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllAttendanceRequestDto {

    @Builder.Default
    private int pageNo = 1;

    @Builder.Default
    private int pageSize = 10;

    private String search;
    private AttendanceStatus status;
    private AttendanceType type;
    private LeaveRequest leaveRequest;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
}