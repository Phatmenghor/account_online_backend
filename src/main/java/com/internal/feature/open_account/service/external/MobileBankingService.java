
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

            // Send SMS notification with account details and activation code (mirrors C#
            // SmsSender observer)
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
            StringBuilder message = new StringBuilder("Your CPBank Account \n");

            if (usdAccount != null && !usdAccount.isEmpty()) {
                message.append("USD:").append(usdAccount).append("\n");
            }

            if (khrAccount != null && !khrAccount.isEmpty()) {
                message.append("KHR:").append(khrAccount).append("\n");
            }

            if (cif != null && !cif.isEmpty()) {
                message.append("CIF : ").append(cif).append("\n");
            }

            if (activationCode != null && !activationCode.isEmpty()) {
                message.append("MB registerCode: ").append(activationCode).append("\n");
            }

            message.append("MB App: http://onelink.to/cpbank");

            soapSmsSender.sendSms(
                    properties.getMb().getOtpUrl(),
                    properties.getMb().getSecretKey(),
                    phone,
                    message.toString());

            log.info("Account SMS sent successfully to phone: {}", phone);

        } catch (Exception e) {
            log.error("Failed to send account SMS (non-critical) to {}: {}", phone, e.getMessage());
        }
    }

    private MobileBankingRequest buildRequest(CustomerRequest request, String cif,
            String khrAccount, String usdAccount) {
        String formattedDob = formatDateOfBirth(request.getDateOfBirth());
        String signData = generateSignature(cif, request.getPhoneNumber());
        String branchCode = request.getBranchCode() != null ? request.getBranchCode()
                : defaultProperties.getBranchCode();
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

        ResponseEntity<MobileBankingResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                MobileBankingResponse.class);

        return response.getBody();
    }

    private String formatDateOfBirth(String dob) {
        try {
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
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().toLowerCase();
        } catch (Exception e) {
            log.error("Failed to generate signature: {}", e.getMessage());
            return "";
        }
    }
}
