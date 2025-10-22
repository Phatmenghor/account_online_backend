package com.internal.feature.camdx.service.ServiceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.internal.config.CpbProperties;
import com.internal.feature.camdx.dto.CamdxFaceRequest;
import com.internal.feature.camdx.dto.CamdxRequest;
import com.internal.feature.camdx.dto.CamdxValidateNidRequest;
import com.internal.feature.camdx.service.CamdxService;
import com.internal.feature.telegram_alerts.service.serviceImpl.CamdxErrorCheckService;
import com.internal.utils.service.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamdxServiceImp implements CamdxService {

    private final CamdxErrorCheckService errorCheckService;
    private final CpbProperties cpbProperties;
    private final HttpClientUtil httpClient;

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

            // 🔥 Extract middleware raw message
            String rawBody = ex.getResponseBodyAsString();

            log.error("NID Validation API error: {}", rawBody);

            // 🔥 Push immediately to Telegram
            errorCheckService.sendInfraErrorAlertFromException(request, rawBody);

            // then rethrow or wrap
            throw ex;
        } catch (Exception e) {
            log.error("Unexpected error calling NID Validation", e);
            errorCheckService.sendInfraErrorAlertFromException(request, e.getMessage());
            throw e;
        }
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