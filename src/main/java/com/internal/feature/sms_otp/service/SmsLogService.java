
package com.internal.feature.sms_otp.service;

import com.internal.feature.sms_otp.models.SmsLog;
import com.internal.feature.sms_otp.repository.SmsLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsLogService {

    private final SmsLogRepository smsLogRepository;

    public void saveLog(SmsLog smsLog) {
        try {
            smsLogRepository.save(smsLog);
        } catch (Exception e) {
            log.error("Failed to save SMS log: {}", e.getMessage());
        }
    }
}