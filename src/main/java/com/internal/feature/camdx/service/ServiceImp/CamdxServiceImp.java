package com.internal.feature.camdx.service.ServiceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.config.CpbProperties;
import com.internal.exceptions.error.custom.NidValidationException;
import com.internal.feature.camdx.dto.CamdxFaceRequest;
import com.internal.feature.camdx.dto.CamdxRequest;
import com.internal.feature.camdx.dto.CamdxValidateNidRequest;
import com.internal.feature.camdx.service.CamdxService;
import com.internal.feature.telegram_alerts.service.serviceImpl.CamdxErrorCheckServiceImpl;
import com.internal.utils.constants.AppConstants;
import com.internal.utils.service.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

import static com.internal.utils.constants.AppConstants.NID_ERROR_SYSTEM;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamdxServiceImp implements CamdxService {

    private final CamdxErrorCheckServiceImpl errorCheckService;
    private final CpbProperties cpbProperties;
    private final HttpClientUtil httpClient;
    private final ObjectMapper objectMapper;

    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(
                cpbProperties.getCamdx().getAuth().getTokenHeaderName(),
                cpbProperties.getCamdx().getAuth().getToken()
        );
        return headers;
    }

    @Override
    public JsonNode validateNid(CamdxValidateNidRequest request) {
        String url = cpbProperties.getCamdx().getUrl() + cpbProperties.getValidate().getNid();
        log.info("Calling NID validation API: {}", url);

        try {
            JsonNode response = httpClient.post(url, request, buildAuthHeaders(), "NID Validation");
            errorCheckService.checkValidationResponse(response, request);
            return response;

        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            String rawBody = ex.getResponseBodyAsString();
            int statusCode = ex.getStatusCode().value();

            log.error("NID Validation API error - Status: {}, Body: {}", statusCode, rawBody);

            try {
                errorCheckService.sendInfraErrorAlertFromException(request, rawBody);
            } catch (Exception e) {
                log.error("Failed to send Telegram notification: {}", e.getMessage());
            }

            String userMessage = getUserFriendlyMessage(statusCode, rawBody);
            throw new NidValidationException(statusCode, userMessage);

        } catch (Exception e) {
            log.error("Unexpected error calling NID Validation", e);

            try {
                errorCheckService.sendInfraErrorAlertFromException(request, e.getMessage());
            } catch (Exception te) {
                log.error("Failed to send Telegram notification: {}", te.getMessage());
            }

            throw new NidValidationException(500, NID_ERROR_SYSTEM);
        }
    }

    private String getUserFriendlyMessage(int statusCode, String rawBody) {
        String uiMessage;

        switch (statusCode) {
            case 400:
                uiMessage = AppConstants.MSG_400;
                break;
            case 420:
                uiMessage = AppConstants.MSG_420;
                break;
            case 500:
                uiMessage = AppConstants.MSG_500;
                break;
            case 501:
                uiMessage = AppConstants.MSG_501;
                break;
            case 502:
                uiMessage = AppConstants.MSG_502;
                break;
            case 503:
                uiMessage = AppConstants.MSG_503;
                break;
            case 504:
                uiMessage = AppConstants.MSG_504;
                break;
            default:
                // Try to parse any message from rawBody JSON
                try {
                    JsonNode json = objectMapper.readTree(rawBody);
                    if (json.has("message") && !json.path("message").asText().isEmpty()) {
                        uiMessage = json.path("message").asText() + " " + AppConstants.SUPPORT_CONTACT;
                    } else {
                        uiMessage = "Unknown error occurred. " + AppConstants.SUPPORT_CONTACT;
                    }
                } catch (Exception e) {
                    log.debug("Could not parse error response body", e);
                    uiMessage = "Unknown error occurred. " + AppConstants.SUPPORT_CONTACT;
                }
                break;
        }

        return uiMessage;
    }

    @Override
    public JsonNode validateNidFace(CamdxRequest request) {
        String url = cpbProperties.getCamdx().getUrl() + cpbProperties.getValidate().getFaceApiRoute();
        log.info("Calling NID face validation API: {}", url);
        return httpClient.post(url, request, buildAuthHeaders(), "Face Validation");
    }

    @Override
    public JsonNode extractNid(CamdxFaceRequest request) {
        String url = cpbProperties.getCamdx().getUrl() + cpbProperties.getOcr().getApiRoute();
        log.info("Calling OCR NID extract API: {}", url);
        return httpClient.post(url, request, buildAuthHeaders(), "OCR Extraction");
    }
}
