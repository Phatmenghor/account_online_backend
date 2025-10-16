package com.internal.feature.openAcc.service;

import com.internal.config.CpbProperties;
import com.internal.feature.openAcc.dto.request.ClsSMS;
import com.internal.feature.openAcc.dto.request.TblSetting;
import com.internal.feature.openAcc.dto.response.OtpRecord;
import com.internal.feature.openAcc.repository.SmsRepository;
import com.internal.utils.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final SmsRepository smsRepository;
    private final OtpGenerator otpGenerator;
    private final CpbProperties cpbProperties;
    private final RestTemplate restTemplate;

    public int processSms(ClsSMS data) {
        boolean isNeedSend = true;
        double remainingSeconds = 0;
        int id = 0;

        // 1️⃣ Check OTP attempts
        Optional<OtpRecord> otpOpt = smsRepository.getLatestOtpRecord(data.getPhone());
        if (otpOpt.isPresent()) {
            OtpRecord record = otpOpt.get();
            if (record.getAttempt() >= 3 && record.getLastAttempt() != null &&
                    Duration.between(record.getLastAttempt(), LocalDateTime.now()).toMinutes() < 5) {
                return -500;
            } else if (record.getAttempt() >= 3) {
                smsRepository.resetOtpAttempt(record.getId());
            }
        }

        // 2️⃣ Get OTP description
        TblSetting setting = smsRepository.getAcctOnlineSetting();
        String descOtp = setting.getSetDesc();

        // 3️⃣ Check recent OTP (cooldown)
        if ("0".equals(data.getApp())) {
            LocalDateTime lastOtpTime = smsRepository.getLastGeneratedOtpTime(data.getPhone());
            if (lastOtpTime != null) {
                long elapsedSeconds = Duration.between(lastOtpTime, LocalDateTime.now()).getSeconds();
                if (elapsedSeconds < cpbProperties.getOtp().getCooldownSeconds()) {
                    isNeedSend = false;
                    remainingSeconds = cpbProperties.getOtp().getCooldownSeconds() - elapsedSeconds;
                }
            }
        }

        // 4️⃣ Generate and save OTP
        if (isNeedSend) {
            String otp = otpGenerator.generate(cpbProperties.getOtp().getLength());
            id = smsRepository.insertOtpSmsAndGetId(data.getPhone(), data.getApp(), data.getText(), Integer.parseInt(otp));

            if ("0".equals(data.getApp())) {
                sendSmsToUser(data.getPhone(), descOtp + " " + otp);
                return Integer.parseInt(otp);
            }
        } else if ("0".equals(data.getApp())) {
            return -(int) Math.ceil(remainingSeconds);
        } else if ("NID".equals(data.getApp())) {
            return id;
        }

        return id;
    }

    private void sendSmsToUser(String phone, String message) {
        String otpUrl = cpbProperties.getMb().getOtpUrl();
        try {
            log.info("Sending SMS via MB gateway: {}, message: {}", phone, message);
            // Example: call external SOAP or REST API here
            restTemplate.postForObject(otpUrl, message, String.class);
        } catch (Exception e) {
            log.error("Failed to send SMS to {} via {}: {}", phone, otpUrl, e.getMessage(), e);
        }
    }
}
