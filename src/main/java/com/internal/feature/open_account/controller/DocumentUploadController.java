package com.internal.feature.open_account.controller;

import com.internal.feature.logs_report.service.CustomerImageService;
import com.internal.feature.open_account.dto.request.Base64UploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/upload")
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadController {

    private final CustomerImageService customerImageService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> uploadBase64(
            @RequestBody Base64UploadRequest request) {

        log.info("Received base64 upload. Type: {}, LegalId: {}",
                request.getType(), request.getLegalId());

        if (request.getFileBase64() == null || request.getFileBase64().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "File data is empty"));
        }

        if (request.getLegalId() == null || request.getLegalId().isBlank()) {
            log.warn("legalId is missing — falling back to UUID-based filename");
        }

        try {
            String filename = buildFilename(request.getType(), request.getLegalId());
            String savedFilename = customerImageService.saveBase64File(
                    request.getFileBase64(), filename, request.getType());

            log.info("Base64 upload saved: {}", savedFilename);
            return ResponseEntity.ok(Collections.singletonMap("filename", savedFilename));

        } catch (Exception e) {
            log.error("Failed to upload base64 document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to upload document"));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadMultipart(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "nid") String type,
            @RequestParam(value = "legalId", required = false) String legalId) {

        log.info("Received multipart upload. Type: {}, LegalId: {}", type, legalId);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "File is empty"));
        }

        try {
            String filename = buildFilename(type, legalId);
            String savedFilename = customerImageService.saveUploadedFile(file, filename);

            log.info("Multipart upload saved: {}", savedFilename);
            return ResponseEntity.ok(Collections.singletonMap("filename", savedFilename));

        } catch (Exception e) {
            log.error("Failed to upload multipart document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to upload document"));
        }
    }

    /**
     * Builds a unique filename.
     * Pattern: {type}_{legalId}_{yyyyMMddHHmmssSSS}_{6charRandom}.jpg
     * Example: nid_250319613_20260313043535123_a3f9c1.jpg
     */
    private String buildFilename(String type, String legalId) {
        String identifier = (legalId != null && !legalId.isBlank())
                ? legalId
                : UUID.randomUUID().toString();
        String prefix = "selfie".equalsIgnoreCase(type) ? "selfie_" : "nid_";
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String random = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 6);
        return prefix + identifier + "_" + timestamp + "_" + random + ".jpg";
    }
}