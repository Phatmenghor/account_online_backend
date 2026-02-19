package com.internal.feature.logs_report.controller;

import com.internal.feature.logs_report.service.CustomerImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer-images")
@RequiredArgsConstructor
@Slf4j
public class CustomerImageController {

    private final CustomerImageService customerImageService;

    @GetMapping("/{type}/{legalId}")
    public ResponseEntity<Resource> getCustomerImage(
            @PathVariable String type,
            @PathVariable String legalId) {

        log.info("Request to get customer image. Type: {}, LegalID: {}", type, legalId);

        Resource resource = null;
        if ("nid".equalsIgnoreCase(type)) {
            resource = customerImageService.getNidImageResourceForEmail(legalId);
        } else if ("selfie".equalsIgnoreCase(type)) {
            resource = customerImageService.getSelfieImageResourceForEmail(legalId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
