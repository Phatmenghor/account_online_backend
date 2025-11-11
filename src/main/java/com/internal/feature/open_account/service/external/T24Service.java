package com.internal.feature.open_account.service.external;

import com.internal.config.CpbProperties;
import com.internal.exceptions.error.openaccount.T24ServiceException;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.service.xml.OpenAccountXmlBuilder;
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
        int retryCount = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                String url = properties.getT24().getUrl() + "/TWS.CPBOAO/services";

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

                Document doc = parseXmlResponse(response.getBody());

                if (hasJmsError(doc)) {
                    log.warn("JMS error detected, attempt {}/{}", attempt, retryCount);
                    throw new T24ServiceException("T24 JMS error after " + retryCount + " attempts");
                }

                return doc;

            } catch (Exception e) {
                lastException = e;
                log.error("T24 error on attempt {}/{}: {}", attempt, retryCount, e.getMessage());
            }
        }

        throw new T24ServiceException("T24 call failed after " + retryCount + " attempts", lastException);
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
}
