package com.internal.utils.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.internal.exceptions.error.custom.ValidateServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpClientUtil {

    private final RestTemplate restTemplate;

    public String postForString(String url, String body, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType(contentType);
        headers.setContentType(new MediaType(mediaType.getType(), mediaType.getSubtype(), StandardCharsets.UTF_8));

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class);

        return response.getBody();
    }

    public <T> JsonNode post(String url, T request, Map<String, String> headers, String apiName) {
        return executeRequest(url, HttpMethod.POST, request, headers, apiName, JsonNode.class);
    }

    public <T> JsonNode post(String url, T request, String apiName) {
        return post(url, request, null, apiName);
    }

    public JsonNode get(String url, Map<String, String> headers, String apiName) {
        return executeRequest(url, HttpMethod.GET, null, headers, apiName, JsonNode.class);
    }

    /**
     * Make a GET request without custom headers
     */
    public JsonNode get(String url, String apiName) {
        return get(url, null, apiName);
    }

    /**
     * Make a POST request returning a specific type
     */
    public <T, R> R post(String url, T request, Map<String, String> headers, String apiName, Class<R> responseType) {
        return executeRequest(url, HttpMethod.POST, request, headers, apiName, responseType);
    }

    /**
     * Core method to execute HTTP requests
     */
    private <T, R> R executeRequest(
            String url,
            HttpMethod method,
            T request,
            Map<String, String> customHeaders,
            String apiName,
            Class<R> responseType) {
        long startTime = System.currentTimeMillis();

        try {
            // Build headers
            HttpHeaders headers = buildHeaders(customHeaders);

            // Create entity
            HttpEntity<T> entity = new HttpEntity<>(request, headers);

            // Log request
            log.debug("{} API Request - Method: {}, URL: {}", apiName, method, url);

            // Make API call
            ResponseEntity<R> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    responseType);

            // Validate response
            R body = response.getBody();
            if (body == null) {
                log.error("{} API returned null response body", apiName);
                throw new ValidateServiceException(apiName + " API returned empty response");
            }

            // Log success
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} API call successful - Duration: {}ms, Status: {}",
                    apiName, duration, response.getStatusCode());

            return body;

        } catch (HttpClientErrorException.Unauthorized ex) {
            return handleUnauthorized(ex, apiName, startTime);

        } catch (HttpClientErrorException.Forbidden ex) {
            return handleForbidden(ex, apiName, startTime);

        } catch (HttpClientErrorException.BadRequest ex) {
            return handleBadRequest(ex, apiName, startTime);

        } catch (HttpClientErrorException ex) {
            return handleHttpClientError(ex, apiName, startTime);

        } catch (org.springframework.web.client.HttpServerErrorException ex) {
            return handleHttpServerError(ex, apiName, startTime);

        } catch (ResourceAccessException ex) {
            return handleResourceAccessError(ex, apiName, startTime);

        } catch (Exception ex) {
            return handleUnexpectedError(ex, apiName, startTime);
        }
    }

    /**
     * Build HTTP headers with optional custom headers and UTF-8 charset
     */
    private HttpHeaders buildHeaders(Map<String, String> customHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8));
        headers.setAcceptCharset(java.util.Collections.singletonList(StandardCharsets.UTF_8));

        // Add custom headers if provided
        if (customHeaders != null && !customHeaders.isEmpty()) {
            customHeaders.forEach(headers::set);
        }

        return headers;
    }

    // Error handling methods
    private <R> R handleHttpServerError(org.springframework.web.client.HttpServerErrorException ex, String apiName,
            long startTime) {
        log.error("{} API Server Error ({}) - Duration: {}ms, Response: {}",
                apiName, ex.getStatusCode(), System.currentTimeMillis() - startTime,
                ex.getResponseBodyAsString());

        String errorMessage = ex.getResponseBodyAsString();
        // Try to check if it has a specific message inside
        try {
            // Simple check if it's a JSON with "message" field
            // We can't use objectMapper here easily without injecting it or parsing
            // manually
            // But let's just use the raw body or a formatted string
            if (errorMessage.contains("\"message\"")) {
                // Leave it to frontend/service to parse if needed, or just include it
            }
        } catch (Exception e) {
            /* ignore */}

        throw new ValidateServiceException(
                String.format("%s API Server Error: %s", apiName, errorMessage),
                ex);
    }

    // Error handling methods
    private <R> R handleUnauthorized(HttpClientErrorException.Unauthorized ex, String apiName, long startTime) {
        log.error("{} API Unauthorized (401) - Duration: {}ms",
                apiName, System.currentTimeMillis() - startTime);
        log.debug("Response body: {}", ex.getResponseBodyAsString());
        throw new ValidateServiceException(
                String.format("%s API authentication failed", apiName),
                ex);
    }

    private <R> R handleForbidden(HttpClientErrorException.Forbidden ex, String apiName, long startTime) {
        log.error("{} API Forbidden (403) - Duration: {}ms",
                apiName, System.currentTimeMillis() - startTime);
        log.debug("Response body: {}", ex.getResponseBodyAsString());
        throw new ValidateServiceException(
                String.format("%s API access denied", apiName),
                ex);
    }

    private <R> R handleBadRequest(HttpClientErrorException.BadRequest ex, String apiName, long startTime) {
        log.error("{} API Bad Request (400) - Duration: {}ms, Response: {}",
                apiName, System.currentTimeMillis() - startTime, ex.getResponseBodyAsString());
        throw new ValidateServiceException(
                String.format("%s API invalid request: %s", apiName, ex.getResponseBodyAsString()),
                ex);
    }

    private <R> R handleHttpClientError(HttpClientErrorException ex, String apiName, long startTime) {
        log.error("{} API HTTP error ({}) - Duration: {}ms, Response: {}",
                apiName, ex.getStatusCode(), System.currentTimeMillis() - startTime,
                ex.getResponseBodyAsString());
        throw new ValidateServiceException(
                String.format("%s API failed with status %s", apiName, ex.getStatusCode()),
                ex);
    }

    private <R> R handleResourceAccessError(ResourceAccessException ex, String apiName, long startTime) {
        log.error("{} API connection failed - Duration: {}ms, Error: {}",
                apiName, System.currentTimeMillis() - startTime, ex.getMessage());
        throw new ValidateServiceException(
                String.format("%s API connection error (timeout or network issue)", apiName),
                ex);
    }

    private <R> R handleUnexpectedError(Exception ex, String apiName, long startTime) {
        log.error("{} API unexpected error - Duration: {}ms, Error: {}",
                apiName, System.currentTimeMillis() - startTime, ex.getMessage(), ex);
        throw new ValidateServiceException(
                String.format("%s API internal error: %s", apiName, ex.getMessage()),
                ex);
    }
}
