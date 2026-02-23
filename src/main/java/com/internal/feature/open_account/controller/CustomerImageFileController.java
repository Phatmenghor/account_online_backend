package com.internal.feature.open_account.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/customer-images")
@Slf4j
public class CustomerImageFileController {

    @Value("${file.upload.directory:/app/customer-image}")
    private String uploadDir;

    /**
     * Serve a customer image file by filename.
     * Filename prefix determines the subfolder: nid_ → /nid/, selfie_ → /selfie/
     * Example: GET /api/v1/public/customer-images/nid_020013239_20260220045614.jpg
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            String subFolder = filename.startsWith("selfie_") ? "selfie" : "nid";
            Path filePath = Paths.get(uploadDir, subFolder, filename);

            if (!Files.exists(filePath)) {
                log.warn("Customer image not found: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            byte[] bytes = Files.readAllBytes(filePath);
            String extension = filename.contains(".png") ? "png" : filename.contains(".webp") ? "webp" : "jpeg";
            MediaType mediaType = MediaType.parseMediaType("image/" + extension);

            log.info("Serving customer image: {} ({} bytes)", filename, bytes.length);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(bytes);

        } catch (IOException e) {
            log.error("Failed to read customer image {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
