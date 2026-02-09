package com.internal.feature.logs_report.controller;

import com.internal.feature.logs_report.service.CustomerImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer-images")
@RequiredArgsConstructor
@Slf4j
public class CustomerImageController {

    private final CustomerImageService customerImageService;

    @PostMapping("/{legalId}/nid")
    public ResponseEntity<byte[]> getNidImage(@PathVariable String legalId) {
        try {
            if (!customerImageService.nidImageExists(legalId)) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = customerImageService.getNidImageBytes(legalId);
            if (bytes == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(bytes);
        } catch (Exception e) {
            log.error("Failed to stream NID image for legalId {}: {}", legalId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{legalId}/selfie")
    public ResponseEntity<byte[]> getSelfieImage(@PathVariable String legalId) {
        try {
            if (!customerImageService.selfieImageExists(legalId)) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = customerImageService.getSelfieImageBytes(legalId);
            if (bytes == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(bytes);
        } catch (Exception e) {
            log.error("Failed to stream Selfie image for legalId {}: {}", legalId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
