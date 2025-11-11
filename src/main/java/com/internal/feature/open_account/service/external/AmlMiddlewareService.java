package com.internal.feature.open_account.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.config.CpbProperties;
import com.internal.feature.open_account.dto.request.CustomerAmlRequest;
import com.internal.feature.open_account.dto.response.AmlResponseDto;
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
     * Call AML API with Bearer token authorization
     */
    public AmlResponseDto CheckAml(CustomerAmlRequest requestBody) {
        String url = properties.getAml().getUrl();
        String bearerToken = properties.getAml().getToken(); // get token from config

        try {
            // Convert DTO to JSON string
            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearerToken); // adds Authorization: Bearer <token>

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            ResponseEntity<AmlResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    AmlResponseDto.class
            );

            log.info("AML API call success: status={}", response.getStatusCodeValue());
            return response.getBody();

        } catch (Exception e) {
            log.error("AML API call failed: {}", e.getMessage(), e);
            // Return a default error object
            return AmlResponseDto.builder()
                    .RiskLevel("Unknown")
                    .ActionTaken(null)
                    .ServiceName("Intuition")
                    .TotalRulesScore(0)
                    .TrxnID(null)
                    .RulesTriggered(new Object[]{})
                    .build();
        }
    }
}
