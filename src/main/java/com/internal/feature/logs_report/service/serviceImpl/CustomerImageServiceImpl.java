package com.internal.feature.logs_report.service.serviceImpl;

import com.internal.feature.logs_report.dto.request.CustomerFileUploadRequestDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.model.CustomerImage;
import com.internal.feature.logs_report.repository.CustomerImageRepository;
import com.internal.feature.logs_report.service.CustomerImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerImageServiceImpl implements CustomerImageService {

    private final CustomerImageRepository customerImageRepository;

    @Value("${app.upload.directory:/app/customer-image}")
    private String uploadDir;

    @Value("${app.upload.nid:/nid}")
    private String nidPath;

    @Value("${app.upload.selfie:/selfie}")
    private String selfiePath;

    /**
     * Save NID and Selfie images for a customer.
     * Only the filenames are stored in the DB to prevent dependency on path config.
     */
    @Override
    public CustomerImageUploadResponseDto saveCustomerImages(CustomerFileUploadRequestDto request) {
        try {
            // Ensure directories exist
            new File(uploadDir + nidPath).mkdirs();
            new File(uploadDir + selfiePath).mkdirs();

            String legalId = request.getLegal_id();

            // Define consistent file names
            String nidFileName = "nid_" + legalId + ".jpg";
            String selfieFileName = "selfie_" + legalId + ".jpg";

            // Build full paths for saving to disk
            String nidFullPath = Paths.get(uploadDir, "nid", nidFileName).toString();
            String selfieFullPath = Paths.get(uploadDir, "selfie", selfieFileName).toString();

            // Save physical files
            saveBase64ToFile(request.getNidImage(), nidFullPath);
            saveBase64ToFile(request.getSelfieImage(), selfieFullPath);

            // Store only file names in DB (not full paths)
            customerImageRepository.save(CustomerImage.builder()
                    .type("NID")
                    .name(nidFileName)
                    .filePath(nidFileName)
                    .build());

            customerImageRepository.save(CustomerImage.builder()
                    .type("SELFIE")
                    .name(selfieFileName)
                    .filePath(selfieFileName)
                    .build());

            log.info("Saved customer images: NID={}, Selfie={}", nidFileName, selfieFileName);

            // Return filenames
            return CustomerImageUploadResponseDto.builder()
                    .nidImagePath(nidFileName)
                    .selfieImagePath(selfieFileName)
                    .build();

        } catch (Exception e) {
            log.error("Failed to save customer images: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving images", e);
        }
    }

    /** Get NID image file as Resource (for email attachment) */
    @Override
    public Resource getNidImageResourceForEmail(String customerId) {
        try {
            String fileName = "nid_" + customerId + ".jpg";
            String filePath = Paths.get(uploadDir, "nid", fileName).toString();

            File file = new File(filePath);
            if (!file.exists()) {
                log.warn("NID image not found for customer: {}", customerId);
                return null;
            }
            log.info("Retrieved NID image for email: {}", filePath);
            return new FileSystemResource(file);

        } catch (Exception e) {
            log.error("Failed to get NID image resource: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Get NID image as byte array */
    @Override
    public byte[] getNidImageBytes(String customerId) {
        try {
            String fileName = "nid_" + customerId + ".jpg";
            Path path = Paths.get(uploadDir, "nid", fileName);

            if (!Files.exists(path)) {
                log.warn("NID image not found for customer: {}", customerId);
                return null;
            }

            byte[] bytes = Files.readAllBytes(path);
            log.info("Retrieved NID image bytes for customer: {} ({} bytes)", customerId, bytes.length);
            return bytes;

        } catch (IOException e) {
            log.error("Failed to read NID image bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Get Selfie image file as Resource (for email attachment) */
    @Override
    public Resource getSelfieImageResourceForEmail(String customerId) {
        try {
            String fileName = "selfie_" + customerId + ".jpg";
            String filePath = Paths.get(uploadDir, "selfie", fileName).toString();

            File file = new File(filePath);
            if (!file.exists()) {
                log.warn("Selfie image not found for customer: {}", customerId);
                return null;
            }

            log.info("Retrieved Selfie image for email: {}", filePath);
            return new FileSystemResource(file);

        } catch (Exception e) {
            log.error("Failed to get Selfie image resource: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Get Selfie image as byte array */
    @Override
    public byte[] getSelfieImageBytes(String customerId) {
        try {
            String fileName = "selfie_" + customerId + ".jpg";
            Path path = Paths.get(uploadDir, "selfie", fileName);

            if (!Files.exists(path)) {
                log.warn("Selfie image not found for customer: {}", customerId);
                return null;
            }

            byte[] bytes = Files.readAllBytes(path);
            log.info("Retrieved Selfie image bytes for customer: {} ({} bytes)", customerId, bytes.length);
            return bytes;

        } catch (IOException e) {
            log.error("Failed to read Selfie image bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Check if NID image exists */
    @Override
    public boolean nidImageExists(String customerId) {
        String fileName = "nid_" + customerId + ".jpg";
        Path path = Paths.get(uploadDir, "nid", fileName);
        return Files.exists(path);
    }

    /** Check if Selfie image exists */
    @Override
    public boolean selfieImageExists(String customerId) {
        String fileName = "selfie_" + customerId + ".jpg";
        Path path = Paths.get(uploadDir, "selfie", fileName);
        return Files.exists(path);
    }

    /** Utility: Save base64 image to file */
    private void saveBase64ToFile(String base64, String filePath) throws Exception {
        if (base64 == null || base64.isEmpty()) return;

        // 1. Strip metadata if present (e.g., "data:image/jpeg;base64,")
        if (base64.contains(",")) {
            int base64Index = base64.indexOf("base64,");
            if (base64Index != -1) {
                base64 = base64.substring(base64Index + 7);
            } else {
                // Fallback: take content after the last comma
                int lastCommaIndex = base64.lastIndexOf(",");
                base64 = base64.substring(lastCommaIndex + 1);
            }
        }

        // 2. Sanitize: Remove all characters not in the Base64 alphabet (A-Z, a-z, 0-9, +, /, =)
        // This handles newlines, spaces, dots (.), etc.
        base64 = base64.replaceAll("[^A-Za-z0-9+/=]", "");

        byte[] decoded = Base64.getDecoder().decode(base64);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(decoded);
        }
    }

    /** Utility: Encode file to Base64 (optional use) */
    private String encodeFileToBase64(String filePath) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return Base64.getEncoder().encodeToString(bytes);
    }
}
