package com.internal.feature.sms_otp.repository;

import com.internal.feature.sms_otp.models.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
}