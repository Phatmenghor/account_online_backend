package com.internal.feature.open_account.controller;

import com.internal.feature.logs_report.service.CustomerImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/upload-document")
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadController {

    private final CustomerImageService customerImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "nid") String type, // nid or selfie
            @RequestParam(value = "legalId", required = false) String legalId) {
        log.info("Received document upload request. Type: {}, LegalId: {}", type, legalId);

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "File is empty"));
            }

            // Generate filename: {type}_{legalId or randomUUID}.jpg
            String identifier = (legalId != null && !legalId.isEmpty()) ? legalId : UUID.randomUUID().toString();
            String prefix = type.equalsIgnoreCase("selfie") ? "selfie_" : "nid_";
            String extension = ".jpg"; // Force jpg for simplicity as used in service, or extract from
                                       // file.getOriginalFilename()

            String filename = prefix + identifier + extension;

            String savedFilename = customerImageService.saveUploadedFile(file, filename);

            log.info("Document uploaded successfully: {}", savedFilename);
            return ResponseEntity.ok(Collections.singletonMap("filename", savedFilename));

        } catch (Exception e) {
            log.error("Failed to upload document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to upload document"));
        }
    }
}
