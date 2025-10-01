package com.internal.feature.attendance.dto.request;

import com.internal.enumation.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequestDto {

    private AttendanceType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
