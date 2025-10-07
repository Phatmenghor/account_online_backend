package com.internal.feature.report_trainee.service;

import com.internal.feature.report_trainee.models.TraineeReport;

public interface TraineeReportTelegramNotificationService {
    
    void sendReportCreatedNotification(TraineeReport report);
    
    void sendReportUpdatedNotification(TraineeReport report);
    
    void sendReportDeletedNotification(Long reportId, String deletedBy);
}