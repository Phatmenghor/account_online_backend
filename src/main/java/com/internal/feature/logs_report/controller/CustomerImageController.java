package com.internal.feature.logs_report.controller;

import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.service.CustomerImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer-images")
@RequiredArgsConstructor
@Slf4j
public class CustomerImageController {

    private final CustomerImageService customerImageService;

    // 🔹 Fetch images by customer ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerImageUploadResponseDto> getCustomerImages(@PathVariable UUID id) {
        try {
            // Here we assume customerId is string, convert to UUID if necessary
            CustomerImageUploadResponseDto imagePaths = customerImageService.getCustomerImageById(id);

            // Optional: Convert to Base64
            String nidBase64 = encodeFileToBase64(imagePaths.getNidImagePath());
            String selfieBase64 = encodeFileToBase64(imagePaths.getSelfieImagePath());

            imagePaths.setNidImagePath(nidBase64);
            imagePaths.setSelfieImagePath(selfieBase64);

            return ResponseEntity.ok(imagePaths);

        } catch (Exception e) {
            log.error("Failed to fetch images for customerId {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private String encodeFileToBase64(String filePath) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    // Optional: Upload API (if needed)
//    @PostMapping("/upload")
//    public ResponseEntity<CustomerImageUploadResponseDto> uploadCustomerImages(
//            @RequestBody CustomerFileUploadRequestDto request
//    ) {
//        CustomerImageUploadResponseDto response = customerImageService.saveCustomerImages(request);
//        return ResponseEntity.ok(response);
//    }
}
