package com.internal.utils;

import com.internal.feature.sms_otp.models.SmsLog;
import com.internal.feature.sms_otp.service.SmsLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SoapSmsSender {

    private final RestTemplate restTemplate;
    private final SmsLogService smsLogService;

    /**
     * Sends a raw SMS message via SOAP API.
     */
    public void sendSms(String url, String secretKey, String phone, String message) {
        String status = "FAILED";
        String responseCode = null;
        String errorMessage = null;
        String requestId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        try {
            if (message != null) {
                message = message.replaceAll("(?i)registCode:\\s*", "").trim();
                message = message.replaceAll(" +", " ");
            }


            String soapXml = "<?xml version=\"1.0\"?>" +
                    "<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope' xmlns:cpb='http://cpbmobile.vnpay.vn'>" +
                    "<soap:Header/>" +
                    "<soap:Body>" +
                    "<cpb:sendSmsNew>" +
                    "<cpb:requestId>" + requestId + "</cpb:requestId>" +
                    "<cpb:keyword>CPBSMS</cpb:keyword>" +
                    "<cpb:mobileNo>" + phone + "</cpb:mobileNo>" +
                    "<cpb:content><![CDATA[" + message + "]]></cpb:content>" +
                    "<cpb:requestTime></cpb:requestTime>" +
                    "<cpb:contentType>9</cpb:contentType>" +
                    "<cpb:secretKey>" + secretKey + "</cpb:secretKey>" +
                    "</cpb:sendSmsNew>" +
                    "</soap:Body>" +
                    "</soap:Envelope>";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/soap+xml"));
            HttpEntity<String> entity = new HttpEntity<>(soapXml, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            responseCode = response.getStatusCode().toString();
            status = "SUCCESS";

            log.info("SMS sent to {} - Response: {}", phone, response.getStatusCode());

        } catch (Exception ex) {
            errorMessage = ex.getMessage() != null && ex.getMessage().length() > 500
                    ? ex.getMessage().substring(0, 500)
                    : ex.getMessage();
            log.error("Failed to send SMS to {} - {}", phone, ex.getMessage(), ex);
        } finally {
            smsLogService.saveLog(SmsLog.builder()
                    .phone(phone)
                    .message(message)
                    .status(status)
                    .responseCode(responseCode)
                    .errorMessage(errorMessage)
                    .requestId(requestId)
                    .build());
        }
    }
}