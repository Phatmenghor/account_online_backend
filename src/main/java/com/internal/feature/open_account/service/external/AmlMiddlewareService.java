package com.internal.feature.open_account.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.config.CpbProperties;
import com.internal.feature.open_account.dto.request.CustomerAmlRequest;
import com.internal.feature.open_account.dto.response.AmlExternalResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
@Slf4j
public class AmlMiddlewareService {

    private final CpbProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Call AML API with Bearer token authorization with full debug logging
     */
    public AmlExternalResponseDto CheckAml(CustomerAmlRequest requestBody) {
        String url = properties.getAml().getUrl();
        String bearerToken = properties.getAml().getToken(); // get token from config

        try {
            // Convert DTO to JSON string
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            log.info("AML Request JSON: {}", jsonRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearerToken);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            // Get raw response as String first
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("AML API call success: status={}", response.getStatusCodeValue());
            String rawBody = response.getBody();
            log.info("AML raw response body: {}", rawBody);

            // Map raw JSON to DTO
            AmlExternalResponseDto amlResponse;
            try {
                amlResponse = objectMapper.readValue(rawBody, AmlExternalResponseDto.class);
                log.info("AML mapped response: RiskLevel={}, TrxnID={}, ActionTaken={}",
                        amlResponse.getRiskLevel(), amlResponse.getTrxnID(), amlResponse.getActionTaken());
            } catch (Exception e) {
                log.error("Failed to map AML response JSON to DTO: {}", e.getMessage(), e);
                // Optionally map as JsonNode for debugging
                JsonNode node = objectMapper.readTree(rawBody);
                log.info("AML response as JsonNode: {}", node.toPrettyString());
                throw new RuntimeException("Failed to map AML response", e);
            }

            return amlResponse;

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
