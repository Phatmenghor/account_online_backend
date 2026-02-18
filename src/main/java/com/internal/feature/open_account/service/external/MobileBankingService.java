
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
        try {
            MobileBankingRequest mbRequest = buildRequest(request, cif, khrAccount, usdAccount);
            MobileBankingResponse mbResponse = callActivatorApi(mbRequest);
            log.info("Mobile banking activation successful for CIF: {}", cif);

            // Send SMS notification with account details and activation code (mirrors C# SmsSender observer)
            String activationCode = mbResponse != null ? mbResponse.getContent() : null;
            sendAccountSms(request.getPhoneNumber(), usdAccount, khrAccount, cif, activationCode);
            return activationCode;
        } catch (Exception e) {
            log.error("Mobile banking activation failed (non-critical): {}", e.getMessage());
            return null;
        }
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
                .email("NA@gmail.com")
                .address("N/A")
                .cifNo(cif)
                .branchCodeCreatedUser(branchCode)
                .posCodeCreatedUser("POS01")
                .createdUser(request.getGivenName())
                .dateOfBirth(formattedDob)
                .telephone(request.getPhoneNumber())
                .cifBranchCode(branchCode)
                .gender(request.getGender())
                .residence("1")
                .accountNumber(accountNumber)
                .accountType("6011")
                .currency(currency)
                .branchCode(branchCode)
                .packageCode("BASIC")
                .telephoneOtp(request.getPhoneNumber())
                .staffCode("123")
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

    private String generateSignature(String cif, String phone) {
        try {
            String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String value = properties.getMb().getSecretKey() + cif + phone + dateNow;

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().toLowerCase();
        } catch (Exception e) {
            log.error("Failed to generate signature: {}", e.getMessage());
            return "";
        }
    }
}
