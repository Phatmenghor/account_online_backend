
package com.internal.feature.open_account.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.config.CpbProperties;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.request.MobileBankingRequest;
import com.internal.feature.open_account.dto.response.MobileBankingResponse;
import com.internal.feature.open_account.models.SmsLog;
import com.internal.feature.open_account.repository.SmsLogRepository;
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
    private final RestTemplate restTemplate;
    private final SmsLogRepository smsLogRepository;
    private final ObjectMapper objectMapper;

    public void activate(CustomerRequest request, String cif, String khrAccount, String usdAccount) {
        try {
            if (!"production".equalsIgnoreCase(properties.getEnvironment())) {
                log.info("Skipping mobile banking activation in non-production");
                return;
            }
            
            MobileBankingRequest mbRequest = buildRequest(request, cif, khrAccount, usdAccount);
            MobileBankingResponse response = callActivatorApi(mbRequest);
            logActivation(request, cif, khrAccount, usdAccount, mbRequest, response);
            
        } catch (Exception e) {
            log.error("Mobile banking activation failed (non-critical): {}", e.getMessage());
        }
    }

    private MobileBankingRequest buildRequest(CustomerRequest request, String cif,
                                              String khrAccount, String usdAccount) {
        String formattedDob = formatDateOfBirth(request.getDateOfBirth());
        String signData = generateSignature(cif, request.getSms());
        String branchCode = request.getBranchCode() != null ? request.getBranchCode() : "KH0012011";
        String accountNumber = usdAccount != null ? usdAccount : khrAccount;
        String currency = usdAccount != null ? "USD" : "KHR";
        
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
            .telephone(request.getSms())
            .cifBranchCode(branchCode)
            .gender(request.getGender())
            .residence("1")
            .accountNumber(accountNumber)
            .accountType("6011")
            .currency(currency)
            .branchCode(branchCode)
            .packageCode("BASIC")
            .telephoneOtp(request.getSms())
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
            MobileBankingResponse.class
        );
        
        return response.getBody();
    }

    private void logActivation(CustomerRequest request, String cif, String khrAccount, 
                               String usdAccount, MobileBankingRequest mbRequest, 
                               MobileBankingResponse response) {
        try {
            String requestJson = objectMapper.writeValueAsString(mbRequest);
            String responseJson = objectMapper.writeValueAsString(response);
            
            SmsLog smsLog = SmsLog.builder()
                .cif(cif)
                .name(request.getShortName())
                .acctIdKhr(khrAccount)
                .acctIdUsd(usdAccount)
                .nid(request.getLegalId())
                .phone(request.getSms())
                .activatorPayload(requestJson)
                .activatorResponse(responseJson)
                .build();
            
            smsLogRepository.save(smsLog);
        } catch (Exception e) {
            log.error("Failed to log mobile banking activation: {}", e.getMessage());
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