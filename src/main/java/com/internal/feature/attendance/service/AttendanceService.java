package com.internal.feature.attendance.service;

import com.internal.feature.attendance.dto.request.ApprovalRequestDto;
import com.internal.feature.attendance.dto.request.AttendanceRequestDto;
import com.internal.feature.attendance.dto.request.AttendanceUpdateRequestDto;
import com.internal.feature.attendance.dto.request.GetAllAttendanceRequestDto;
import com.internal.feature.attendance.dto.resposne.AllAttendanceResponseDto;
import com.internal.feature.attendance.dto.resposne.AttendanceResponseDto;

import java.util.List;

public interface AttendanceService {

    AttendanceResponseDto createAttendanceRequest(AttendanceRequestDto requestDto);

    AllAttendanceResponseDto getAllAttendances(GetAllAttendanceRequestDto requestDto);

    List<AttendanceResponseDto> getAllListAttendances(GetAllAttendanceRequestDto requestDto);

    AllAttendanceResponseDto getMyAttendances(GetAllAttendanceRequestDto requestDto);

    AttendanceResponseDto getAttendanceById(Long id);

    AttendanceResponseDto updateAttendance(Long id, AttendanceUpdateRequestDto requestDto);

    AttendanceResponseDto cancelAttendance(Long id);

    void deleteAttendance(Long id);

    AttendanceResponseDto approveOrRejectAttendance(Long id, ApprovalRequestDto approvalDto);
}
