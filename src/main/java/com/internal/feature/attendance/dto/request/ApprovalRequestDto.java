package com.internal.feature.attendance.dto.request;

import com.internal.enumation.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDto {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private AttendanceStatus status;

    private String approvalNotes;
}