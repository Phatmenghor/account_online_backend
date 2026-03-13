package com.internal.feature.open_account.controller;

import com.internal.feature.logs_report.service.serviceImpl.CustomerImageServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer-images")
@RequiredArgsConstructor
@Slf4j
public class CustomerImageFileController {

    private final CustomerImageServiceImpl customerImageService;

    /**
     * Serve a customer image by filename.
     * Searches both flat (old) and week-subfolder (new) structure automatically.
     *
     * Examples:
     *   GET /api/customer-images/nid_250319613_20260313043535123_a3f9c1.jpg
     *   GET /api/customer-images/selfie_250319613_20260313043535123_a3f9c1.jpg
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            String subFolder = filename.startsWith("selfie_") ? "selfie" : "nid";

            Optional<Path> found = customerImageService.findFileByName(subFolder, filename);

            if (found.isEmpty()) {
                log.warn("Customer image not found: {} in subFolder: {}", filename, subFolder);
                return ResponseEntity.notFound().build();
            }

            Path filePath = found.get();
            byte[] bytes = Files.readAllBytes(filePath);
            MediaType mediaType = resolveMediaType(filename);

            log.info("Serving customer image: {} ({} bytes) from week: {}",
                    filename, bytes.length, filePath.getParent().getFileName());

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(bytes);

        } catch (IOException e) {
            log.error("Failed to read customer image {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private MediaType resolveMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return MediaType.IMAGE_PNG;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }
}