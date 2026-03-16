package com.internal.feature.open_account.service.external;

import com.internal.config.CpbProperties;
import com.internal.exceptions.error.openaccount.T24ServiceException;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.service.xml.OpenAccountXmlBuilder;
import com.internal.feature.telegram_alerts.config.TelegramService;
import com.internal.feature.telegram_alerts.service.MonitoringService;
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
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class T24Service {

    private final CpbProperties properties;
    private final RestTemplate restTemplate;
    private final OpenAccountXmlBuilder xmlBuilder;
    private final TelegramService telegramService;
    private final MonitoringService monitoringService;

    // text/xml; charset=UTF-8 — ensures Khmer and other non-Latin characters
    // are not mangled by the HTTP layer defaulting to ISO-8859-1
    private static final MediaType TEXT_XML_UTF8 =
            new MediaType("text", "xml", StandardCharsets.UTF_8);

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
                operation, url, xmlRequest != null ? xmlRequest.getBytes(StandardCharsets.UTF_8).length : 0);

        try {
            HttpHeaders headers = new HttpHeaders();
            // UTF-8 charset declared on Content-Type so T24 parses Khmer characters correctly
            headers.setContentType(TEXT_XML_UTF8);
            headers.set("Accept", "text/xml; charset=UTF-8");
            headers.set("SOAPAction", operation);

            // Send as raw UTF-8 bytes — bypasses any RestTemplate re-encoding
            byte[] requestBytes = xmlRequest.getBytes(StandardCharsets.UTF_8);
            HttpEntity<byte[]> entity = new HttpEntity<>(requestBytes, headers);

            // Receive as byte[] so we control UTF-8 decoding ourselves
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    byte[].class);

            long duration = System.currentTimeMillis() - startTime;

            byte[] responseBytes = response.getBody();
            // Decode response explicitly as UTF-8
            String responseBody = responseBytes != null
                    ? new String(responseBytes, StandardCharsets.UTF_8)
                    : null;

            int responseSize = responseBody != null ? responseBody.length() : 0;

            log.info("T24 Response Received | op={} | status={} | responseSize={} bytes | duration={} ms",
                    operation, response.getStatusCode(), responseSize, duration);
            log.info("T24 Response Body: {}", responseBody);

            checkAndAlertSecurityViolation(responseBody, operation);

            if (responseBody == null || responseBody.isEmpty()) {
                log.error("T24 Empty Response | op={} | status={}", operation, response.getStatusCode());
                throw new T24ServiceException("T24 returned an empty response");
            }

            Document doc = parseXmlResponse(responseBody);

            if (XmlParser.hasError(doc)) {
                String errorMessage = XmlParser.extractErrorMessage(doc);
                log.error("T24 Business Error | op={} | message={}", operation, errorMessage);
                throw new T24ServiceException("T24 Error: " + errorMessage);
            }

            if (hasJmsError(doc)) {
                log.warn("T24 JMS Error Detected | op={}", operation);
                throw new T24ServiceException("T24 JMS error");
            }

            return doc;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("T24 Request Failed | op={} | url={} | duration={} ms | message={}",
                    operation, url, duration, e.getMessage(), e);

            // Monitor: T24 Error
            monitoringService.logT24Error(operation, "N/A", e.getMessage());

            throw new T24ServiceException("T24 call failed", e);
        }
    }

    private Document parseXmlResponse(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Encode to bytes as UTF-8 explicitly — avoids JVM default charset corrupting Khmer
        return builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
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