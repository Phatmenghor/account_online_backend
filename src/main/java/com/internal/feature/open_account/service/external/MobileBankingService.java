
package com.internal.feature.open_account.service.external;

import com.internal.config.CpbProperties;
import com.internal.config.DefaultProperties;
import com.internal.feature.logs_report.model.CifActivationLog;
import com.internal.feature.logs_report.service.CifActivationLogService;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.request.MobileBankingRequest;
import com.internal.feature.open_account.dto.response.MobileBankingResponse;
import com.internal.utils.SoapSmsSender;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileBankingService {

    private final CpbProperties properties;
    private final DefaultProperties defaultProperties;
    private final RestTemplate restTemplate;

    @Qualifier("mobileBankingRestTemplate")
    private final RestTemplate mobileBankingRestTemplate;

    private final SoapSmsSender soapSmsSender;
    private final CifActivationLogService cifActivationLogService;

    public String activate(CustomerRequest request, String cif, String khrAccount, String usdAccount) {
        log.info("Activating mobile banking for CIF: {}", cif);
        String activationCode = null;
        try {
            MobileBankingRequest mbRequest = buildRequest(request, cif, khrAccount, usdAccount);
            MobileBankingResponse mbResponse = callActivatorApi(mbRequest, cif);
            log.info("Mobile banking activation successful for CIF: {}", cif);
            activationCode = mbResponse != null ? mbResponse.getContent() : null;
        } catch (Exception e) {
            log.error("Mobile banking activation failed (non-critical): {}", e.getMessage());
        }

        // Always send account SMS regardless of MB activation result
        sendAccountSms(request.getPhoneNumber(), usdAccount, khrAccount, cif, activationCode);
        return activationCode;
    }

    private void sendAccountSms(String phone, String usdAccount, String khrAccount, String cif, String activationCode) {
        try {
            StringBuilder message = new StringBuilder();

            message.append("Welcome to CPBank!\r\n");
            message.append("Your new account details:\r\n");

            if (usdAccount != null && !usdAccount.isEmpty()) {
                message.append("USD Account: ").append(usdAccount).append("\r\n");
            }
            if (khrAccount != null && !khrAccount.isEmpty()) {
                message.append("KHR Account: ").append(khrAccount).append("\r\n");
            }
            if (cif != null && !cif.isEmpty()) {
                message.append("CIF: ").append(cif).append("\r\n");
            }
            if (activationCode != null && !activationCode.isEmpty()) {
                activationCode = activationCode.replaceAll("(?i)registCode:\\s*", "").trim();
                message.append("MB Activation Code: ").append(activationCode).append("\r\n");
            }
            message.append("Download CPBank App: http://onelink.to/cpbank");

            log.info("Start send SMS to phone: {}", phone);

            soapSmsSender.sendSms(
                    properties.getMb().getOtpUrl(),
                    properties.getMb().getSecretKey(),
                    phone,
                    message.toString()
            );

            log.info("Account SMS sent successfully to phone: {}", phone);

        } catch (Exception e) {
            log.error("Failed to send account SMS (non-critical) to {}: {}", phone, e.getMessage());
        }
    }

    private MobileBankingRequest buildRequest(CustomerRequest request, String cif,
                                              String khrAccount, String usdAccount) {
        String formattedDob = formatDateOfBirth(request.getDateOfBirth());
        String signData = generateSignature(cif, request.getPhoneNumber());
//        String branchCode = "KH0011090"; // Hardcoded for testing as requested
        String branchCode = request.getBranchCode() != null ? request.getBranchCode()
                : defaultProperties.getBranchCode();
        String accountNumber = usdAccount != null ? usdAccount : khrAccount;
        String currency = usdAccount != null ? AppConstants.CURRENCY_USD : AppConstants.CURRENCY_KHR;

        return MobileBankingRequest.builder()
                .customerName(request.getFamilyName() + " " + request.getGivenName())
                .customerType("100")
                .identityNumber(request.getLegalId().trim())
                .email(request.getEmail() != null ? request.getEmail() : "NA@gmail.com")
                .address(request.getLegalAddress())
                .cifNo(cif)
                .branchCodeCreatedUser(branchCode)
                .posCodeCreatedUser("POS01")
                .createdUser(request.getGivenName())
                .dateOfBirth(formattedDob)
                .telephone(request.getPhoneNumber())
                .cifBranchCode(branchCode)
                .gender(request.getGender())
                .residence(request.getResidence() != null ? request.getResidence() : "1")
                .accountNumber(accountNumber)
                .accountType("6011")
                .currency(currency)
                .branchCode(branchCode)
                .packageCode("BASIC")
                .telephoneOtp(request.getPhoneNumber())
                .staffCode(request.getReferralId() != null ? request.getReferralId() : "")
                .signData(signData)
                .channel("INTERNET BANKING")
                .mobileChannel("I")
                .build();
    }

    private MobileBankingResponse callActivatorApi(MobileBankingRequest request, String cif) {
        int maxRetries = 3;
        int[] retryDelays = {2000, 4000, 8000, 16000}; // Exponential backoff: 2s, 4s, 8s, 16s

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return attemptApiCall(request, cif, attempt, maxRetries);
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    // Final attempt failed, throw error
                    throw e;
                }
                // Retry with exponential backoff
                int delayMs = retryDelays[attempt - 1];
                log.warn("Attempt {} failed for CIF {}: {}. Retrying in {}ms...",
                        attempt, cif, e.getMessage(), delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        return null;
    }

    private MobileBankingResponse attemptApiCall(MobileBankingRequest request, String cif, int attemptNumber, int totalAttempts) {
        String url = properties.getMb().getRegisterCodeUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MobileBankingRequest> entity = new HttpEntity<>(request, headers);

        long startTime = System.currentTimeMillis();
        String requestPayload = null;
        String responsePayload = null;
        String errorCode = null;
        String errorMessage = null;
        boolean success = false;
        MobileBankingResponse parsed = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            requestPayload = mapper.writeValueAsString(request);

            log.info("Calling Activator API (Attempt {}/{}): {}", attemptNumber, totalAttempts, url);
            log.info("Request: {}", requestPayload);

            ResponseEntity<String> rawResponse = mobileBankingRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            log.info("Response Status: {}", rawResponse.getStatusCode());
            log.info("Raw Response Body: {}", rawResponse.getBody());

            responsePayload = rawResponse.getBody();

            if (responsePayload == null) {
                errorMessage = "Empty response body from MB activation API";
                return null;
            }

            parsed = mapper.readValue(responsePayload, MobileBankingResponse.class);
            log.info("Parsed Response: code={}, message={}, content={}", parsed.getCode(), parsed.getMessage(),
                    parsed.getContent());

            if (parsed.getCode() != null && !"00".equals(parsed.getCode())) {
                errorCode = parsed.getCode();
                errorMessage = parsed.getMessage();
                throw new RuntimeException(
                        "Mobile banking API error - code: " + parsed.getCode() + ", message: " + parsed.getMessage());
            }

            success = true;
            return parsed;

        } catch (Exception e) {
            log.error("Error calling Activator API (Attempt {}/{}): {}", attemptNumber, totalAttempts, e.getMessage(), e);
            if (errorMessage == null) {
                errorMessage = e.getMessage();
            }
            throw new RuntimeException(e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String activationCode = (parsed != null) ? parsed.getContent() : null;

            CifActivationLog activationLog = CifActivationLog.builder()
                    .cifNo(cif)
                    .telephone(request.getTelephone())
                    .accountNo(request.getAccountNumber())
                    .currency(request.getCurrency())
                    .branchCode(request.getBranchCode())
                    .channel(request.getChannel())
                    .requestPayload(requestPayload)
                    .responsePayload(responsePayload)
                    .errorCode(errorCode)
                    .errorMessage(errorMessage != null && errorMessage.length() > 500
                            ? errorMessage.substring(0, 500)
                            : errorMessage)
                    .activationCode(activationCode)
                    .isSuccess(success)
                    .durationMs(duration)
                    .build();

            cifActivationLogService.saveLog(activationLog);
        }
    }

    private String formatDateOfBirth(String dob) {
        if (dob == null || dob.isEmpty())
            return dob;
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // Try yyyyMMdd (e.g. "20030919")
        try {
            LocalDate date = LocalDate.parse(dob, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return date.format(outputFormatter);
        } catch (Exception ignored) {
        }
        // Try yyyy-MM-dd (e.g. "2003-09-19")
        try {
            LocalDate date = LocalDate.parse(dob, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date.format(outputFormatter);
        } catch (Exception ignored) {
        }
        return dob;
    }

    // Matches C# MobileService.CreateMD5Hash: MD5(secretKey + cif + sms + dateNow)
    // using ASCII encoding
    private String generateSignature(String cif, String sms) {
        try {
            String dateNow = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String value = "mobilebankingCPB@#%123" + cif + sms + dateNow;

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md5.digest(value.getBytes(StandardCharsets.US_ASCII));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02X", b));
            }

            return sb.toString().toLowerCase();
        } catch (Exception e) {
            log.error("Failed to generate signature: {}", e.getMessage());
            return "";
        }
    }
}
