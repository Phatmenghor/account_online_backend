package com.internal.feature.attendance.service;

import com.internal.feature.attendance.models.AttendanceEntity;

public interface TelegramNotificationService {
    
    void sendAttendanceRequestNotification(AttendanceEntity attendance);
    
    void sendAttendanceApprovalNotification(AttendanceEntity attendance);
    
    void sendAttendanceRejectionNotification(AttendanceEntity attendance);
}