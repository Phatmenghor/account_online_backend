package com.internal.feature.sms_otp.service.impl;

import com.internal.config.CpbProperties;
import com.internal.exceptions.error.otp.OtpAttemptsExceededException;
import com.internal.exceptions.error.otp.OtpCooldownException;
import com.internal.exceptions.error.otp.OtpInvalidException;
import com.internal.exceptions.error.otp.OtpNotFoundException;
import com.internal.feature.sms_otp.dto.request.SendOtpRequest;
import com.internal.feature.sms_otp.dto.request.VerifyOtpRequest;
import com.internal.feature.sms_otp.dto.response.SendOtpResponse;
import com.internal.feature.sms_otp.dto.response.VerifyOtpResponse;
import com.internal.feature.sms_otp.mapper.SmsOtpMapper;
import com.internal.feature.sms_otp.models.OtpSms;
import com.internal.feature.sms_otp.repository.OtpRepository;
import com.internal.feature.sms_otp.service.OtpService;
import com.internal.utils.OtpGenerator;
import com.internal.utils.constants.AppConstants;
import com.internal.utils.service.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final OtpGenerator otpGenerator;
    private final SmsOtpMapper otpMapper;
    private final CpbProperties cpbProperties;
    private final HttpClientUtil httpClient;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public SendOtpResponse sendOtp(SendOtpRequest request) {
        String phone = request.getPhone();
        log.info("Processing OTP request for phone: {}", phone);

        // 1. Check cooldown period FIRST (before lockout check)
        checkCooldownPeriod(phone);

        // 2. Check for recent attempts and lockout
        checkAttemptLockout(phone);

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

        // 6. Send SMS (don't let SMS failure block the flow)
        try {
            sendSmsToUser(phone, otpCode);
        } catch (Exception e) {
            log.error("SMS sending failed but OTP was saved - Phone: {}, OTP ID: {}", phone, otpSms.getId(), e);
            // Consider: Do you want to throw exception here or just log and continue?
            // If SMS is critical, uncomment the next line:
            // throw new SmsDeliveryException("Failed to send OTP via SMS", e);
        }

        return otpMapper.toSendOtpResponse(otpSms);
    }

    @Override
    @Transactional(noRollbackFor = {OtpInvalidException.class, OtpAttemptsExceededException.class})
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String phone = request.getPhone();
        String otpCode = request.getOtpCode();
        log.info("Processing OTP verification for phone: {}", phone);

        // 1. Get the latest active OTP first (single query)
        OtpSms latestOtp = otpRepository.findLatestActiveOtpByPhone(phone)
                .orElseThrow(() -> new OtpNotFoundException(phone));

        log.info("DEBUG - Found OTP - ID: {}, Code: {}, Current attempts: {}, Status: {}",
                latestOtp.getId(), latestOtp.getOtpCode(), latestOtp.getAttempt(), latestOtp.getStatus());

        // 2. Check if code matches and OTP is not expired
        boolean isValid = latestOtp.getOtpCode().equals(otpCode)
                && latestOtp.getExpiresAt().isAfter(LocalDateTime.now());

        if (!isValid) {
            // 3. Calculate remaining attempts BEFORE incrementing
            int remainingAttempts = AppConstants.MAX_ATTEMPTS - latestOtp.getAttempt();

            if (remainingAttempts <= 0) {
                // User is already locked out, calculate remaining time
                long secondsSinceLastAttempt = Duration.between(
                        latestOtp.getLastAttempt(), LocalDateTime.now()
                ).getSeconds();
                long lockoutSeconds = AppConstants.LOCKOUT_MINUTES * 60;
                long remainingSeconds = Math.max(0, lockoutSeconds - secondsSinceLastAttempt);

                log.warn("Max OTP attempts exceeded for phone: {}, remaining: {}s", phone, remainingSeconds);
                throw new OtpAttemptsExceededException(remainingSeconds);
            }

            // 4. Increment attempt counter for failed verification (only if not locked out)
            incrementFailedAttempt(latestOtp);

            // Recalculate after increment
            remainingAttempts = AppConstants.MAX_ATTEMPTS - latestOtp.getAttempt();

            if (remainingAttempts <= 0) {
                // Just hit max attempts now, full lockout period applies
                long lockoutSeconds = AppConstants.LOCKOUT_MINUTES * 60;
                log.warn("Max OTP attempts just exceeded for phone: {}", phone);
                throw new OtpAttemptsExceededException(lockoutSeconds);
            }

            log.warn("Invalid OTP for phone: {}, remaining attempts: {}", phone, remainingAttempts);
            throw new OtpInvalidException(remainingAttempts);
        }

        // 5. Mark as verified (keeps record in history)
        latestOtp.setStatus(1); // 1 = verified
        latestOtp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(latestOtp);

        log.info("OTP verified successfully - ID: {}, Phone: {}", latestOtp.getId(), phone);

        return otpMapper.toVerifyOtpResponse(latestOtp);
    }

    /**
     * Check if user is locked out due to too many failed attempts
     * FIXED: Only check lockout if attempts >= MAX_ATTEMPTS
     */
    private void checkAttemptLockout(String phone) {
        Optional<OtpSms> latestOtp = otpRepository.findLatestActiveOtpByPhone(phone);

        if (!latestOtp.isPresent()) {
            // No previous OTP, no lockout
            return;
        }

        OtpSms otp = latestOtp.get();

        // CRITICAL FIX: Only apply lockout if max attempts reached AND lastAttempt exists
        if (otp.getAttempt() >= AppConstants.MAX_ATTEMPTS && otp.getLastAttempt() != null) {
            long secondsSinceLastAttempt = Duration.between(
                    otp.getLastAttempt(), LocalDateTime.now()
            ).getSeconds();

            long lockoutSeconds = AppConstants.LOCKOUT_MINUTES * 60;

            if (secondsSinceLastAttempt < lockoutSeconds) {
                long remainingSeconds = lockoutSeconds - secondsSinceLastAttempt;
                long remainingMinutes = remainingSeconds / 60;
                long remainingSecondsOnly = remainingSeconds % 60;

                log.warn("User locked out - Phone: {}, Remaining: {}m {}s",
                        phone, remainingMinutes, remainingSecondsOnly);

                // Pass remaining seconds to exception for better UX
                throw new OtpAttemptsExceededException(remainingSeconds);
            } else {
                // Lockout period expired, will be reset when old OTP is expired
                log.info("Lockout period expired for phone: {}", phone);
                // Don't need to manually reset here as expireAllActiveOtpsByPhone will handle it
            }
        }
        // If attempts < MAX_ATTEMPTS, user is not locked out, allow new OTP
    }

    /**
     * Check if cooldown period has elapsed since last OTP
     * FIXED: Check creation time from ANY OTP, not just active ones
     */
    private void checkCooldownPeriod(String phone) {
        // Note: Repository query should use .findFirstByPhoneOrderByCreatedAtDesc()
        // or add LIMIT 1 in the @Query to avoid NonUniqueResultException
        Optional<LocalDateTime> lastOtpTime = otpRepository.findLastOtpCreationTime(phone);

        if (!lastOtpTime.isPresent()) {
            // No previous OTP, no cooldown
            return;
        }

        long elapsedSeconds = Duration.between(lastOtpTime.get(), LocalDateTime.now()).getSeconds();
        long cooldownSeconds = cpbProperties.getOtp().getCooldownSeconds();

        if (elapsedSeconds < cooldownSeconds) {
            int remainingSeconds = (int) (cooldownSeconds - elapsedSeconds);
            log.warn("Cooldown active - Phone: {}, Remaining: {}s", phone, remainingSeconds);
            throw new OtpCooldownException(remainingSeconds);
        }
    }

    /**
     * Increment failed verification attempt counter
     * FIXED: Clear cache and detach entity to force fresh fetch next time
     */
    private void incrementFailedAttempt(OtpSms otp) {
        int newAttemptCount = otp.getAttempt() + 1;
        log.info("DEBUG - Before increment - ID: {}, Old attempts: {}, New attempts: {}",
                otp.getId(), otp.getAttempt(), newAttemptCount);

        otp.setAttempt(newAttemptCount);
        otp.setLastAttempt(LocalDateTime.now());
        OtpSms saved = otpRepository.saveAndFlush(otp);

        // CRITICAL: Clear the persistence context to force fresh DB reads
        entityManager.clear();

        log.info("DEBUG - After save - ID: {}, Saved attempts: {}",
                saved.getId(), saved.getAttempt());

        log.info("Failed attempt recorded - Phone: {}, Total attempts: {}",
                otp.getPhone(), otp.getAttempt());
    }

    /**
     * Send SMS via external gateway using HttpClientUtil
     */
    private void sendSmsToUser(String phone, String otpCode) {
        // Skip in non-production environments
        if (!AppConstants.ENV_PRODUCTION.equalsIgnoreCase(cpbProperties.getEnvironment())) {
            log.info("Skipping SMS send (non-production) - Phone: {}, OTP: {}", phone, otpCode);
            return;
        }

        String otpUrl = cpbProperties.getMb().getOtpUrl();
        String message = cpbProperties.getOtp().getMessage() + " " + otpCode;

        log.info("Sending SMS via gateway - Phone: {}", phone);

        // Create SMS request payload
        Map<String, String> smsRequest = new HashMap<>();
        smsRequest.put("phone", phone);
        smsRequest.put("message", message);

        // Send via HttpClientUtil with proper error handling
        httpClient.post(otpUrl, smsRequest, "SMS Gateway");

        log.info("SMS sent successfully to: {}", phone);
    }
}