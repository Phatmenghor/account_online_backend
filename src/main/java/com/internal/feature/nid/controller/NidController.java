package com.internal.feature.nid.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.nid.dto.request.EkycFaceRequest;
import com.internal.feature.nid.dto.request.EkycRequest;
import com.internal.feature.nid.dto.request.ValidateNidRequest;
import com.internal.feature.nid.service.NidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/v1/public/validate-nid")
@RequiredArgsConstructor
@Slf4j
public class NidController {

    private final NidService nidService;

    /**
     * Validate NID Information
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> validateNid(
            @Valid @RequestBody ValidateNidRequest request) {
        log.info("➡️ [validateNid] Received request: {}", request);

        JsonNode response = nidService.validateNid(request);

        log.info("✅ [validateNid] Validation successful for ID: {}", request.getIdNumber());
        log.debug("🔍 [validateNid] Response: {}", response);

        return ResponseEntity.ok(ApiResponse.success("NID validated successfully!", response));
    }

    /**
     * Validate NID Face (Face matching between ID photo and live capture)
     */
    @PostMapping("/face")
    public ResponseEntity<ApiResponse<?>> validateNidFace(
            @Valid @RequestBody EkycRequest request) {
        log.info("➡️ [validateNidFace] Received face validation request");

        JsonNode response = nidService.validateNidFace(request);

        log.info("✅ [validateNidFace] Face validation completed successfully");
        log.debug("🔍 [validateNidFace] Response: {}", response);

        return ResponseEntity.ok(ApiResponse.success("NID Face validated successfully!", response));
    }

    /**
     * Extract NID information via OCR
     */
    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<?>> extractNid(
            @Valid @RequestBody EkycFaceRequest request) {
        log.info("➡️ [extractNid] Received Extract NID request");

        JsonNode response = nidService.extractNid(request);

        log.info("✅ [extractNid] NID extraction completed successfully");
        log.debug("🔍 [extractNid] Response: {}", response);

        return ResponseEntity.ok(ApiResponse.success("NID extract successfully!", response));
    }
}
