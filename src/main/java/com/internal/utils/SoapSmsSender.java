package com.internal.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class SoapSmsSender {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendSms(String url, String secretKey, String phone, String message) {
        try {
            String requestId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

            String soapXml =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope' xmlns:cpb='http://cpbmobile.vnpay.vn'>" +
                            "<soap:Header/>" +
                            "<soap:Body>" +
                            "<cpb:sendSmsNew>" +
                            "<cpb:requestId>" + requestId + "</cpb:requestId>" +
                            "<cpb:keyword>CPBSMS</cpb:keyword>" +
                            "<cpb:mobileNo>" + phone + "</cpb:mobileNo>" +
                            "<cpb:content>" + message + "</cpb:content>" +
                            "<cpb:requestTime></cpb:requestTime>" +
                            "<cpb:contentType>9</cpb:contentType>" +
                            "<cpb:secretKey>" + secretKey + "</cpb:secretKey>" +
                            "</cpb:sendSmsNew>" +
                            "</soap:Body>" +
                            "</soap:Envelope>";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/soap+xml; charset=utf-8"));
            HttpEntity<String> entity = new HttpEntity<>(soapXml, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            log.info("âœ… SMS sent to {} - Response: {}", phone, response.getStatusCode());
        } catch (Exception ex) {
            log.error("âŒ Failed to send SMS to {} - {}", phone, ex.getMessage(), ex);
        }
    }
}
