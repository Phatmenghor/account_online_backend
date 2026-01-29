package com.internal.feature.open_account.service.external;

import com.internal.config.CpbProperties;
import com.internal.exceptions.error.openaccount.T24ServiceException;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.service.xml.OpenAccountXmlBuilder;
import com.internal.feature.telegram_alerts.config.TelegramService;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class T24Service {

    private final CpbProperties properties;
    private final RestTemplate restTemplate;
    private final OpenAccountXmlBuilder xmlBuilder;
    private final TelegramService telegramService;

    public Document createCustomer(CustomerRequest request) {
        log.info("Creating customer in T24 for Legal ID: {}", request.getLegalId());

        String xmlRequest = xmlBuilder.buildCustomerCreationXml(request);
        return executeT24Request(xmlRequest, "OAOCUSTOMERCREATION");
    }

    public Document createAccount(CustomerRequest request, String cif, String currency) {
        log.info("Creating {} account in T24 for CIF: {}", currency, cif);

        String xmlRequest = xmlBuilder.buildAccountCreationXml(request, cif, currency);
        return executeT24Request(xmlRequest, "ACCREATIONOAO");
    }

    private Document executeT24Request(String xmlRequest, String operation) {
        long startTime = System.currentTimeMillis();
        String url = properties.getT24().getUrl() + "/TWS.CPBOAO/services";

        log.info("T24 Request Started | op={} | url={} | payloadSize={} bytes",
                operation, url, xmlRequest != null ? xmlRequest.length() : 0);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", operation);

            HttpEntity<String> entity = new HttpEntity<>(xmlRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            long duration = System.currentTimeMillis() - startTime;

            String responseBody = response.getBody();
            int responseSize = responseBody != null ? responseBody.length() : 0;

            log.info("T24 Response Received | op={} | status={} | responseSize={} bytes | duration={} ms",
                    operation, response.getStatusCode(), responseSize, duration
            );
            log.info("T24 Response Body: {}", responseBody);

            checkAndAlertSecurityViolation(responseBody, operation);

            if (responseBody == null || responseBody.isEmpty()) {
                log.error("T24 Empty Response | op={} | status={}", operation, response.getStatusCode());
                throw new T24ServiceException("T24 returned an empty response");
            }

            Document doc = parseXmlResponse(responseBody);

            if (hasJmsError(doc)) {
                log.warn("T24 JMS Error Detected | op={}", operation);
                throw new T24ServiceException("T24 JMS error");
            }

            return doc;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("T24 Request Failed | op={} | url={} | duration={} ms | message={}",
                    operation, url, duration, e.getMessage(), e);

            throw new T24ServiceException("T24 call failed", e);
        }
    }

    private Document parseXmlResponse(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
    }

    private boolean hasJmsError(Document doc) {
        try {
            String docString = doc.toString();
            return docString.contains("JMS Message redelivered") && docString.contains("T24Error");
        } catch (Exception e) {
            return false;
        }
    }

    private void checkAndAlertSecurityViolation(String responseBody, String operation) {
        if (responseBody != null && responseBody.contains(AppConstants.T24_ACCOUNT_ERROR)) {
            String errorMessage = String.format("🚨 **T24 Security Violation Detected**\n\n" +
                            "**Operation:** `%s`\n" +
                            "**Error:** `SECURITY VIOLATION DURING SIGN ON PROCESS`\n" +
                            "**Action Required:** Check T24 credentials in `application.yaml`.",
                    operation);
            telegramService.sendMarkdownAclInternalMessage(errorMessage);
            log.error("T24 Security Violation Detected! Alert sent to Telegram.");
        }
    }
}
