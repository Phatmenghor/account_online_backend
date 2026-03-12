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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        checkCooldownPeriod(phone);
        checkAttemptLockout(phone);

        otpRepository.expireAllActiveOtpsByPhone(phone);

        String otpCode = otpGenerator.generate();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(cpbProperties.getOtp().getExpiryMinutes());

        OtpSms otpSms = OtpSms.builder()
                .phone(phone)
                .otpCode(otpCode)
                .attempt(0)
                .status(0)
                .expiresAt(expiresAt)
                .build();

        otpSms = otpRepository.save(otpSms);
        log.info("OTP created successfully - ID: {}, Phone: {}", otpSms.getId(), phone);

        if (AppConstants.DEFAULT_DEV_OTP.equals(otpCode)) {
            log.info("Skipping SMS sending for default OTP: {}", otpCode);
        } else {
            try {
                sendSmsToUser(phone, otpCode);
            } catch (Exception e) {
                log.error("SMS sending failed but OTP was saved - Phone: {}, OTP ID: {}", phone, otpSms.getId(), e);
            }
        }

        return otpMapper.toSendOtpResponse(otpSms);
    }

    @Override
    @Transactional(noRollbackFor = {OtpInvalidException.class, OtpAttemptsExceededException.class})
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String phone = request.getPhone();
        String otpCode = request.getOtpCode();
        log.info("Processing OTP verification for phone: {}", phone);

        OtpSms latestOtp = otpRepository.findLatestActiveOtpByPhone(phone)
                .orElseThrow(() -> new OtpNotFoundException(phone));

        boolean isValid = latestOtp.getOtpCode().equals(otpCode)
                && latestOtp.getExpiresAt().isAfter(LocalDateTime.now());

        if (!isValid) {
            int remainingAttempts = AppConstants.MAX_ATTEMPTS - latestOtp.getAttempt();

            if (remainingAttempts <= 0) {
                long secondsSinceLastAttempt = Duration.between(
                        latestOtp.getLastAttempt(), LocalDateTime.now()
                ).getSeconds();
                long lockoutSeconds = AppConstants.LOCKOUT_MINUTES * 60;
                long remainingSeconds = Math.max(0, lockoutSeconds - secondsSinceLastAttempt);
                throw new OtpAttemptsExceededException(remainingSeconds);
            }

            incrementFailedAttempt(latestOtp);
            remainingAttempts = AppConstants.MAX_ATTEMPTS - latestOtp.getAttempt();

            if (remainingAttempts <= 0) {
                long lockoutSeconds = AppConstants.LOCKOUT_MINUTES * 60;
                throw new OtpAttemptsExceededException(lockoutSeconds);
            }

            throw new OtpInvalidException(remainingAttempts);
        }

        latestOtp.setStatus(1);
        latestOtp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(latestOtp);

        log.info("OTP verified successfully - ID: {}, Phone: {}", latestOtp.getId(), phone);

        return otpMapper.toVerifyOtpResponse(latestOtp);
    }

    private void checkAttemptLockout(String phone) {
        Optional<OtpSms> latestOtp = otpRepository.findLatestActiveOtpByPhone(phone);
        if (latestOtp.isEmpty()) return;

        OtpSms otp = latestOtp.get();

        if (otp.getAttempt() >= AppConstants.MAX_ATTEMPTS && otp.getLastAttempt() != null) {
            long secondsSinceLastAttempt = Duration.between(
                    otp.getLastAttempt(), LocalDateTime.now()
            ).getSeconds();

            long lockoutSeconds = AppConstants.LOCKOUT_MINUTES * 60;

            if (secondsSinceLastAttempt < lockoutSeconds) {
                long remainingSeconds = lockoutSeconds - secondsSinceLastAttempt;
                throw new OtpAttemptsExceededException(remainingSeconds);
            }
        }
    }

    private void checkCooldownPeriod(String phone) {
        Optional<LocalDateTime> lastOtpTime = otpRepository.findLastOtpCreationTime(phone);
        if (lastOtpTime.isEmpty()) return;

        long elapsedSeconds = Duration.between(lastOtpTime.get(), LocalDateTime.now()).getSeconds();
        long cooldownSeconds = cpbProperties.getOtp().getCooldownSeconds();

        if (elapsedSeconds < cooldownSeconds) {
            int remainingSeconds = (int) (cooldownSeconds - elapsedSeconds);
            throw new OtpCooldownException(remainingSeconds);
        }
    }

    private void incrementFailedAttempt(OtpSms otp) {
        otp.setAttempt(otp.getAttempt() + 1);
        otp.setLastAttempt(LocalDateTime.now());
        otpRepository.saveAndFlush(otp);
        entityManager.clear();
    }

    /**
     * Send SMS via SOAP Gateway (fixed to avoid JSON converter issue)
     */
    private void sendSmsToUser(String phone, String otpCode) {
        String otpUrl = cpbProperties.getMb().getOtpUrl();
        String secretKey = cpbProperties.getMb().getSecretKey();
        String requestID = String.valueOf(System.currentTimeMillis());
        String message = cpbProperties.getOtp().getMessage() + " " + otpCode;

        String soapXml = "<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope' "
                + "xmlns:cpb='http://cpbmobile.vnpay.vn'>"
                + "<soap:Header/>"
                + "<soap:Body>"
                + "<cpb:sendSmsNew>"
                + "<cpb:requestId>" + requestID + "</cpb:requestId>"
                + "<cpb:keyword>CPBSMS</cpb:keyword>"
                + "<cpb:mobileNo>" + phone + "</cpb:mobileNo>"
                + "<cpb:content>" + message + "</cpb:content>"
                + "<cpb:requestTime></cpb:requestTime>"
                + "<cpb:contentType>9</cpb:contentType>"
                + "<cpb:secretKey>" + secretKey + "</cpb:secretKey>"
                + "</cpb:sendSmsNew>"
                + "</soap:Body>"
                + "</soap:Envelope>";

        try {
            log.info("Sending SOAP SMS request - Phone: {}, RequestID: {}", phone, requestID);

            // Use raw XML POST, return response as String (no JSON parsing)
            String responseXml = httpClient.postForString(otpUrl, soapXml, "application/soap+xml");

            // Extract <return> JSON payload from SOAP response (handles namespace prefixes e.g. <ns:return>)
            Matcher matcher = Pattern.compile("<(?:\\w+:)?return>(.*?)</(?:\\w+:)?return>").matcher(responseXml);
            String jsonPayload = matcher.find() ? matcher.group(1) : null;

            log.info("SOAP SMS raw response: {}", responseXml);
            if (jsonPayload != null) {
                log.info("SOAP SMS extracted payload: {}", jsonPayload);
            }

            if (jsonPayload != null && jsonPayload.contains("\"rescode\":\"00\"")) {
                log.info("SMS sent successfully to: {}", phone);
            } else {
                log.warn("SMS sending may have failed - Phone: {}, Response: {}", phone, jsonPayload);
            }

        } catch (Exception e) {
            log.info("Failed to send SOAP SMS - Phone: {}, RequestID: {}", phone, requestID, e);
        }
    }
}
