package com.internal.feature.openAcc.service.impl;

import com.internal.config.CpbProperties;
import com.internal.exceptions.error.otp.OtpAttemptsExceededException;
import com.internal.exceptions.error.otp.OtpCooldownException;
import com.internal.exceptions.error.otp.OtpInvalidException;
import com.internal.exceptions.error.otp.OtpNotFoundException;
import com.internal.feature.openAcc.dto.request.SendOtpRequest;
import com.internal.feature.openAcc.dto.request.VerifyOtpRequest;
import com.internal.feature.openAcc.dto.response.SendOtpResponse;
import com.internal.feature.openAcc.dto.response.VerifyOtpResponse;
import com.internal.feature.openAcc.mapper.OtpMapper;
import com.internal.feature.openAcc.models.OtpSms;
import com.internal.feature.openAcc.repository.OtpRepository;
import com.internal.feature.openAcc.service.OtpService;
import com.internal.utils.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final OtpGenerator otpGenerator;
    private final OtpMapper otpMapper;
    private final CpbProperties cpbProperties;

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_MINUTES = 5;

    @Override
    @Transactional
    public SendOtpResponse sendOtp(SendOtpRequest request) {
        String phone = request.getPhone();
        log.info("Processing OTP request for phone: {}", phone);

        // 1. Check for recent attempts and lockout
        checkAttemptLockout(phone);

        // 2. Check cooldown period
        checkCooldownPeriod(phone);

        // 3. Expire all previous active OTPs for this phone (keeps history)
        otpRepository.expireAllActiveOtpsByPhone(phone);

        // 4. Generate new OTP
        String otpCode = otpGenerator.generate();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(cpbProperties.getOtp().getExpiryMinutes());

        // 5. Save OTP to database (creates new record, keeps old ones)
        OtpSms otpSms = OtpSms.builder()
                .phone(phone)
                .otpCode(otpCode)
                .attempt(0)
                .status(0) // 0 = active
                .expiresAt(expiresAt)
                .build();

        otpSms = otpRepository.save(otpSms);
        log.info("OTP created successfully - ID: {}, Phone: {}", otpSms.getId(), phone);

        // 6. Send SMS
        sendSmsToUser(phone, otpCode);

        return otpMapper.toSendOtpResponse(otpSms);
    }

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String phone = request.getPhone();
        String otpCode = request.getOtpCode();
        log.info("Processing OTP verification for phone: {}", phone);

        // 1. Find the latest active OTP
        Optional<OtpSms> otpOpt = otpRepository.findValidOtpByPhoneAndCode(
                phone, otpCode, LocalDateTime.now()
        );

        if (!otpOpt.isPresent()) {
            // 2. Increment attempt counter for failed verification
            incrementFailedAttempt(phone);
            
            // 3. Get remaining attempts
            OtpSms latestOtp = otpRepository.findLatestActiveOtpByPhone(phone)
                    .orElseThrow(() -> new OtpNotFoundException(phone));
            
            int remainingAttempts = MAX_ATTEMPTS - latestOtp.getAttempt();
            
            if (remainingAttempts <= 0) {
                log.warn("Max OTP attempts exceeded for phone: {}", phone);
                throw new OtpAttemptsExceededException(LOCKOUT_MINUTES);
            }
            
            log.warn("Invalid OTP for phone: {}, remaining attempts: {}", phone, remainingAttempts);
            throw new OtpInvalidException(remainingAttempts);
        }

        OtpSms otpSms = otpOpt.get();

        // 4. Mark as verified (keeps record in history)
        otpSms.setStatus(1); // 1 = verified
        otpSms.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otpSms);

        log.info("OTP verified successfully - ID: {}, Phone: {}", otpSms.getId(), phone);

        return otpMapper.toVerifyOtpResponse(otpSms);
    }

    /**
     * Check if user is locked out due to too many failed attempts
     */
    private void checkAttemptLockout(String phone) {
        Optional<OtpSms> latestOtp = otpRepository.findLatestActiveOtpByPhone(phone);
        
        if (latestOtp.isPresent()) {
            OtpSms otp = latestOtp.get();
            
            if (otp.getAttempt() >= MAX_ATTEMPTS && otp.getLastAttempt() != null) {
                long minutesSinceLastAttempt = Duration.between(
                        otp.getLastAttempt(), LocalDateTime.now()
                ).toMinutes();
                
                if (minutesSinceLastAttempt < LOCKOUT_MINUTES) {
                    log.warn("User locked out - Phone: {}, Minutes remaining: {}", 
                            phone, LOCKOUT_MINUTES - minutesSinceLastAttempt);
                    throw new OtpAttemptsExceededException(LOCKOUT_MINUTES);
                } else {
                    // Reset attempts after lockout period expired
                    otp.setAttempt(0);
                    otp.setLastAttempt(null);
                    otpRepository.save(otp);
                    log.info("Lockout period expired, reset attempts for phone: {}", phone);
                }
            }
        }
    }

    /**
     * Check if cooldown period has elapsed since last OTP
     */
    private void checkCooldownPeriod(String phone) {
        Optional<LocalDateTime> lastOtpTime = otpRepository.findLastOtpCreationTime(phone);
        
        if (lastOtpTime.isPresent()) {
            long elapsedSeconds = Duration.between(lastOtpTime.get(), LocalDateTime.now()).getSeconds();
            long cooldownSeconds = cpbProperties.getOtp().getCooldownSeconds();
            
            if (elapsedSeconds < cooldownSeconds) {
                int remainingSeconds = (int) (cooldownSeconds - elapsedSeconds);
                log.warn("Cooldown active - Phone: {}, Remaining: {}s", phone, remainingSeconds);
                throw new OtpCooldownException(remainingSeconds);
            }
        }
    }

    /**
     * Increment failed verification attempt counter
     */
    private void incrementFailedAttempt(String phone) {
        Optional<OtpSms> otpOpt = otpRepository.findLatestActiveOtpByPhone(phone);
        
        if (otpOpt.isPresent()) {
            OtpSms otp = otpOpt.get();
            otp.setAttempt(otp.getAttempt() + 1);
            otp.setLastAttempt(LocalDateTime.now());
            otpRepository.save(otp);
            log.info("Failed attempt recorded - Phone: {}, Total attempts: {}", phone, otp.getAttempt());
        }
    }

    /**
     * Send SMS via external gateway
     */
    private void sendSmsToUser(String phone, String otpCode) {
        String otpUrl = cpbProperties.getMb().getOtpUrl();
        String message = cpbProperties.getOtp().getMessage() + " " + otpCode;
        
        try {
            log.info("Sending SMS - Phone: {}, OTP: {}", phone, otpCode);
            // TODO: Implement actual SMS gateway integration
            // Example: restTemplate.postForObject(otpUrl, smsRequest, String.class);
            log.info("SMS sent successfully to: {}", phone);
        } catch (Exception e) {
            log.error("Failed to send SMS - Phone: {}, Error: {}", phone, e.getMessage(), e);
            // Don't throw exception - OTP is already saved in DB
            // User can still verify if they receive SMS eventually
        }
    }
}