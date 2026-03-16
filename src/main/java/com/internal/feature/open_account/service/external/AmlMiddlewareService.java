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
                log.error("AML Service Error Simulation Enabled");
                throw new ValidateServiceException(AppConstants.MSG_SYSTEM_BUSY);
            }

            if (simulateAmlHighRisk) {
                log.warn("AML High Risk Simulation Enabled - returning simulated high-risk response");
                AmlExternalResponseDto simulated = AmlExternalResponseDto.builder()
                        .riskLevel("HIGH")
                        .actionTaken("Review Required")
                        .rulesTriggered("[{\"RuleName\":\"Sanction List Hit\"}]")
                        .serviceName("Simulation")
                        .totalRulesScore(100)
                        .trxnID("SIM-" + System.currentTimeMillis())
                        .build();
                log.warn("Simulated AML Response: RiskLevel={}, TrxnID={}", simulated.getRiskLevel(), simulated.getTrxnID());
                return simulated;
            }

            String url = properties.getAml().getUrl();
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            log.debug("AML Request to URL: {} | Customer: {}", url, requestBody.getCustomerId());

            String credentials = properties.getAml().getUsername() + ":" + properties.getAml().getPassword();
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON.getType(),
                    MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedCredentials);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            long duration = System.currentTimeMillis() - startTime;

            String rawBody = response.getBody();
            log.debug("AML API Response ({}ms): {}", duration, rawBody);

            if (rawBody == null || rawBody.trim().isEmpty()) {
                throw new RuntimeException("AML Service returned empty response");
            }

            Map<String, Object> map = objectMapper.readValue(rawBody, new TypeReference<Map<String, Object>>() {
            });
            Object rulesArray = map.get("RulesTriggered");
            String rulesAsString = rulesArray == null ? "" : objectMapper.writeValueAsString(rulesArray);
            map.put("RulesTriggered", rulesAsString);

            AmlExternalResponseDto result = objectMapper.convertValue(map, AmlExternalResponseDto.class);
            log.info("AML Check Completed ({}ms) | RiskLevel: {} | TrxnID: {} | RulesScore: {}",
                    duration, result.getRiskLevel(), result.getTrxnID(), result.getTotalRulesScore());

            return result;

        } catch (Exception e) {
            log.error("AML API call failed: {} | Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new ValidateServiceException(AppConstants.MSG_SYSTEM_BUSY);
        }
    }
}