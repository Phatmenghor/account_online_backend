package com.internal.feature.open_account.service.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.config.CpbProperties;
import com.internal.exceptions.error.custom.ValidateServiceException;
import com.internal.utils.constants.AppConstants;
import com.internal.feature.open_account.dto.request.CustomerAmlRequest;
import com.internal.feature.open_account.dto.response.AmlExternalResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmlMiddlewareService {

    private final CpbProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${simulator.aml.error:false}")
    private boolean simulateAmlServiceError;

    @Value("${simulator.aml.high-risk:false}")
    private boolean simulateAmlHighRisk;

    public AmlExternalResponseDto CheckAml(CustomerAmlRequest requestBody) {

        try {
            if (simulateAmlServiceError) {
                throw new ValidateServiceException(AppConstants.MSG_SYSTEM_BUSY);
            }

            if (simulateAmlHighRisk) {
                log.info("Simulating AML High Risk Response");
                return AmlExternalResponseDto.builder()
                        .riskLevel("HIGH")
                        .actionTaken("Review Required")
                        .rulesTriggered("[{\"RuleName\":\"Sanction List Hit\"}]")
                        .serviceName("Simulation")
                        .totalRulesScore(100)
                        .trxnID("SIM-" + System.currentTimeMillis())
                        .build();
            }

            String url = properties.getAml().getUrl();
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            log.info("AML Request JSON: {}", jsonRequest);

            String credentials = properties.getAml().getUsername() + ":" + properties.getAml().getPassword();
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodedCredentials);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            String rawBody = response.getBody();
            log.info("AML raw response body: {}", rawBody);

            if (rawBody == null || rawBody.trim().isEmpty()) {
                throw new RuntimeException("AML Service returned empty response");
            }

            Map<String, Object> map = objectMapper.readValue(rawBody, new TypeReference<Map<String, Object>>() {
            });
            Object rulesArray = map.get("RulesTriggered");
            String rulesAsString = rulesArray == null ? "" : objectMapper.writeValueAsString(rulesArray);
            map.put("RulesTriggered", rulesAsString);

            return objectMapper.convertValue(map, AmlExternalResponseDto.class);

        } catch (Exception e) {
            log.error("AML API call failed: {}", e.getMessage(), e);
            throw new ValidateServiceException(AppConstants.MSG_SYSTEM_BUSY);
        }
    }
}