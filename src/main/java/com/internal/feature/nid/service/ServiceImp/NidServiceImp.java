package com.internal.feature.nid.service.ServiceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.internal.exceptions.error.ValidateServiceException;
import com.internal.feature.nid.dto.request.EkycFaceRequest;
import com.internal.feature.nid.dto.request.EkycRequest;
import com.internal.feature.nid.dto.request.ValidateNidRequest;
import com.internal.config.CpbProperties;
import com.internal.feature.nid.service.NidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NidServiceImp implements NidService {

    private final CpbProperties cpbProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    // Centralized POST request handler returning JsonNode
    private <T> JsonNode postRequest(String url, T request, String apiName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(cpbProperties.getCamdx().getAuth().getTokenHeaderName(),
                    cpbProperties.getCamdx().getAuth().getToken());

            HttpEntity<T> entity = new HttpEntity<>(request, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
            return response.getBody();

        } catch (HttpClientErrorException.Unauthorized ex) {
            log.error("{} API Unauthorized: {}", apiName, ex.getResponseBodyAsString(), ex);
            throw new ValidateServiceException(apiName + " API Unauthorized", ex);

        } catch (HttpClientErrorException ex) {
            log.error("{} API HTTP error: {}", apiName, ex.getResponseBodyAsString(), ex);
            throw new ValidateServiceException(apiName + " API HTTP error: " + ex.getStatusCode(), ex);

        } catch (Exception ex) {
            log.error("{} API Unexpected error: {}", apiName, ex.getMessage(), ex);
            throw new ValidateServiceException(apiName + " API internal error", ex);
        }
    }

    @Override
    public JsonNode validateNid(ValidateNidRequest request) {
        String requestUrl = cpbProperties.getCamdx().getUrl() + cpbProperties.getValidate().getNid();
        log.info("Calling NID validation API: {}", requestUrl);
        return postRequest(requestUrl, request, "NID Validation");
    }

    @Override
    public JsonNode validateNidFace(EkycRequest request) {
        String requestUrl = cpbProperties.getCamdx().getUrl() + cpbProperties.getValidate().getFaceApiRoute();
        log.info("Calling NID face validation API: {}", requestUrl);
        return postRequest(requestUrl, request, "Face Validation");
    }

    @Override
    public JsonNode extractNid(EkycFaceRequest request) {
        String requestUrl = cpbProperties.getCamdx().getUrl() + cpbProperties.getOcr().getApiRoute();
        log.info("Calling OCR NID extract API: {}", requestUrl);
        return postRequest(requestUrl, request, "OCR Extraction");
    }
}
