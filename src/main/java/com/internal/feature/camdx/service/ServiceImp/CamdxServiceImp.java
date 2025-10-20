package com.internal.feature.camdx.service.ServiceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.internal.feature.camdx.dto.CamdxFaceRequest;
import com.internal.feature.camdx.dto.CamdxRequest;
import com.internal.feature.camdx.dto.CamdxValidateNidRequest;
import com.internal.config.CpbProperties;
import com.internal.feature.camdx.service.CamdxService;
import com.internal.utils.service.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamdxServiceImp implements CamdxService {

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
        return httpClient.post(url, request, buildAuthHeaders(), "NID Validation");
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