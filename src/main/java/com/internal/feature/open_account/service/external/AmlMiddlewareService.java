package com.internal.feature.open_account.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.config.CpbProperties;
import com.internal.feature.open_account.dto.request.CustomerAmlRequest;
import com.internal.feature.open_account.dto.response.AmlExternalResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmlMiddlewareService {

    private final CpbProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final boolean DEV_FORCE_HIGH_RISK = true; // toggle on/off

    public AmlExternalResponseDto CheckAml(CustomerAmlRequest requestBody) {
        // ✅ Dev override
        if (DEV_FORCE_HIGH_RISK) {
            log.info("DEV override: returning HIGH risk for Legal ID {}", requestBody.getCustomerId());
            return AmlExternalResponseDto.builder()
                    .riskLevel("High")
                    .actionTaken("BLOCK")
                    .totalRulesScore(100)
                    .trxnID("DEV-TRXN-001")
                    .rulesTriggered(new Object[]{"Rule1", "Rule2"})
                    .serviceName("MockService")
                    .build();
        }

        // Original real API logic
        try {
            String url = properties.getAml().getUrl();
            String bearerToken = properties.getAml().getToken();
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            log.info("AML Request JSON: {}", jsonRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearerToken);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );

            String rawBody = response.getBody();
            log.info("AML raw response body: {}", rawBody);

            return objectMapper.readValue(rawBody, AmlExternalResponseDto.class);

        } catch (Exception e) {
            log.error("AML API call failed: {}", e.getMessage(), e);
            return AmlExternalResponseDto.builder()
                    .riskLevel("Unknown")
                    .actionTaken(null)
                    .serviceName("Intuition")
                    .totalRulesScore(0)
                    .trxnID(null)
                    .rulesTriggered(new Object[]{})
                    .build();
        }
    }
}
