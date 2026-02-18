
package com.internal.feature.open_account.service.external;

import com.internal.config.CpbProperties;
import com.internal.config.DefaultProperties;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.request.MobileBankingRequest;
import com.internal.feature.open_account.dto.response.MobileBankingResponse;
import com.internal.utils.SoapSmsSender;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileBankingService {

    private final CpbProperties properties;
    private final DefaultProperties defaultProperties;
    private final RestTemplate restTemplate;
    private final SoapSmsSender soapSmsSender;

    public String activate(CustomerRequest request, String cif, String khrAccount, String usdAccount) {
        log.info("Activating mobile banking for CIF: {}", cif);
        String activationCode = null;
        try {
            MobileBankingRequest mbRequest = buildRequest(request, cif, khrAccount, usdAccount);
            MobileBankingResponse mbResponse = callActivatorApi(mbRequest);
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
                message.append("MB Activation Code: ").append(activationCode).append("\r\n");
            }

            message.append("Download CPBank App: http://onelink.to/cpbank");

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
        String branchCode = request.getBranchCode() != null ? request.getBranchCode() : defaultProperties.getBranchCode();
        String accountNumber = usdAccount != null ? usdAccount : khrAccount;
        String currency = usdAccount != null ? AppConstants.CURRENCY_USD : AppConstants.CURRENCY_KHR;

        return MobileBankingRequest.builder()
                .customerName(request.getFamilyName() + " " + request.getGivenName())
                .customerType("100")
                .identityNumber(request.getLegalId().trim())
                .email(request.getEmail() != null ? request.getEmail() : "NA@gmail.com")
                .address(request.getLegalAddress() != null ? request.getLegalAddress() : "N/A")
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
                .build();
    }

    private MobileBankingResponse callActivatorApi(MobileBankingRequest request) {
        String url = properties.getMb().getRegisterCodeUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MobileBankingRequest> entity = new HttpEntity<>(request, headers);

        try {
            log.info("Calling Activator API: {}", url);
            log.info("Request: {}", request);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Response Status: {}", rawResponse.getStatusCode());
            log.info("Raw Response Body: {}", rawResponse.getBody());

            if (rawResponse.getBody() == null) {
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MobileBankingResponse parsed = mapper.readValue(rawResponse.getBody(), MobileBankingResponse.class);
            log.info("Parsed Response: code={}, message={}, content={}", parsed.getCode(), parsed.getMessage(), parsed.getContent());

            if (parsed.getCode() != null && !"00".equals(parsed.getCode())) {
                throw new RuntimeException("Mobile banking API error - code: " + parsed.getCode() + ", message: " + parsed.getMessage());
            }

            return parsed;

        } catch (Exception e) {
            log.error("Error calling Activator API: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String formatDateOfBirth(String dob) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime date = LocalDateTime.parse(dob + "0000", DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            return date.format(outputFormatter);
        } catch (Exception e) {
            return dob;
        }
    }

    // Matches C# MobileService.CreateMD5Hash: MD5(secretKey + cif + sms + dateNow) using ASCII encoding
    private String generateSignature(String cif, String sms) {
        try {
            String dateNow = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String value = properties.getMb().getSecretKey() + cif + sms + dateNow;

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
