package com.internal.feature.camdx.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.camdx.dto.CamdxFaceRequest;
import com.internal.feature.camdx.dto.CamdxValidateNidRequest;
import com.internal.feature.camdx.service.CamdxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/public/nid")
@RequiredArgsConstructor
@Slf4j
public class CamdxController {
    private final CamdxService nidService;

    /**
     * Validate NID Information
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<?>> validateNid(@Valid @RequestBody CamdxValidateNidRequest request) {
        log.info("Processing NID validation request for ID: {}", request.getIdNumber());

        JsonNode response = nidService.validateNid(request);

        log.info("NID validation completed successfully for ID: {}", request.getIdNumber());
        return ResponseEntity.ok(ApiResponse.success("NID validated successfully", response));
    }

    /**
     * Validate NID Face matching (ID photo vs live capture)
     */
//    @PostMapping("/validate-face")
//    public ResponseEntity<ApiResponse<?>> validateNidFace(@Valid @RequestBody CamdxRequest request) {
//        log.info("Processing NID face validation request");
//
//        JsonNode response = nidService.validateNidFace(request);
//
//        log.info("NID face validation completed successfully");
//        return ResponseEntity.ok(ApiResponse.success("Face validation successful", response));
//    }

    /**
     * Extract NID information via OCR
     */
    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<?>> extractNid(@Valid @RequestBody CamdxFaceRequest request) {
        log.info("Processing NID OCR extraction request");

        JsonNode response = nidService.extractNid(request);

        log.info("NID OCR extraction completed successfully");
        return ResponseEntity.ok(ApiResponse.success("NID extracted successfully", response));
    }
}
