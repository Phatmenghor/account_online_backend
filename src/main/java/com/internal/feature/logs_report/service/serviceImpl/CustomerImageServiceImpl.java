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
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerImageServiceImpl implements CustomerImageService {

    private final CustomerImageRepository customerImageRepository;

    @Value("${app.upload.directory:/app/customer-image}")
    private String uploadDir;

    @Override
    public CustomerImageUploadResponseDto saveCustomerImages(CustomerFileUploadRequestDto request) {
        try {
            // Ensure main directories exist
            File mainDir = new File(uploadDir);
            if (!mainDir.exists()) mainDir.mkdirs();

            // Ensure subfolders exist
            File nidDir = new File(Paths.get(uploadDir, "nid").toString());
            if (!nidDir.exists()) nidDir.mkdirs();

            File selfieDir = new File(Paths.get(uploadDir, "selfie").toString());
            if (!selfieDir.exists()) selfieDir.mkdirs();

            // Save NID Image
            String nidFileName = request.getLegal_id() + ".jpg";
            String nidFilePath = Paths.get(uploadDir, "nid", nidFileName).toString();
            saveBase64ToFile(request.getNidImage(), nidFilePath);

            customerImageRepository.save(CustomerImage.builder()
                    .type("NID")
                    .name(nidFileName)
                    .filePath(nidFilePath)
                    .build());

            // Save Selfie Image
            String selfieFileName = request.getLegal_id() + ".jpg";
            String selfieFilePath = Paths.get(uploadDir, "selfie", selfieFileName).toString();
            saveBase64ToFile(request.getSelfieImage(), selfieFilePath);

            customerImageRepository.save(CustomerImage.builder()
                    .type("SELFIE")
                    .name(selfieFileName)
                    .filePath(selfieFilePath)
                    .build());

            log.info("Saved customer images: NID={}, Selfie={}", nidFilePath, selfieFilePath);

            // Return file paths
            return CustomerImageUploadResponseDto.builder()
                    .nidImagePath(nidFilePath)
                    .selfieImagePath(selfieFilePath)
                    .build();

        } catch (Exception e) {
            log.error("Failed to save customer images: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving images", e);
        }
    }

    @Transactional
    @Override
    public CustomerImageUploadResponseDto getCustomerImageById(UUID id) {
        try {
            CustomerImage nidImage = customerImageRepository.findByTypeAndId("NID", id)
                    .orElseThrow(() -> new RuntimeException("NID image not found"));

            CustomerImage selfieImage = customerImageRepository.findByTypeAndId("SELFIE", id)
                    .orElseThrow(() -> new RuntimeException("Selfie image not found"));

            return CustomerImageUploadResponseDto.builder()
                    .nidImagePath(encodeFileToBase64(nidImage.getFilePath()))
                    .selfieImagePath(encodeFileToBase64(selfieImage.getFilePath()))
                    .build();

        } catch (Exception e) {
            log.error("Failed to read customer images: {}", e.getMessage(), e);
            throw new RuntimeException("Error reading images", e);
        }
    }

    /**
     * Get NID image file as Resource for email attachment/inline embedding
     * @param customerId Customer ID or legal_id
     * @return FileSystemResource of the NID image
     */
    @Override
    public Resource getNidImageResourceForEmail(String customerId) {
        try {
            String nidFileName = customerId + ".jpg";
            String nidFilePath = Paths.get(uploadDir, "nid", nidFileName).toString();

            File nidFile = new File(nidFilePath);
            if (!nidFile.exists()) {
                log.warn("NID image not found for customer: {}", customerId);
                return null;
            }

            log.info("Retrieved NID image for email: {}", nidFilePath);
            return new FileSystemResource(nidFile);

        } catch (Exception e) {
            log.error("Failed to get NID image resource: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get NID image as byte array for email embedding
     * @param customerId Customer ID or legal_id
     * @return Byte array of the image, or null if not found
     */
    @Override
    public byte[] getNidImageBytes(String customerId) {
        try {
            String nidFileName = customerId + ".jpg";
            Path nidFilePath = Paths.get(uploadDir, "nid", nidFileName);

            if (!Files.exists(nidFilePath)) {
                log.warn("NID image not found for customer: {}", customerId);
                return null;
            }

            byte[] imageBytes = Files.readAllBytes(nidFilePath);
            log.info("Retrieved NID image bytes for customer: {} (size: {} bytes)", customerId, imageBytes.length);
            return imageBytes;

        } catch (IOException e) {
            log.error("Failed to read NID image bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get Selfie image file as Resource for email attachment/inline embedding
     * @param customerId Customer ID or legal_id
     * @return FileSystemResource of the Selfie image
     */
    @Override
    public Resource getSelfieImageResourceForEmail(String customerId) {
        try {
            String selfieFileName = customerId + ".jpg";
            String selfieFilePath = Paths.get(uploadDir, "selfie", selfieFileName).toString();

            File selfieFile = new File(selfieFilePath);
            if (!selfieFile.exists()) {
                log.warn("Selfie image not found for customer: {}", customerId);
                return null;
            }

            log.info("Retrieved Selfie image for email: {}", selfieFilePath);
            return new FileSystemResource(selfieFile);

        } catch (Exception e) {
            log.error("Failed to get Selfie image resource: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if NID image exists for a customer
     * @param customerId Customer ID or legal_id
     * @return true if image exists, false otherwise
     */
    @Override
    public boolean nidImageExists(String customerId) {
        String nidFileName = customerId + ".jpg";
        Path nidFilePath = Paths.get(uploadDir, "nid", nidFileName);
        return Files.exists(nidFilePath);
    }

    /**
     * Check if Selfie image exists for a customer
     * @param customerId Customer ID or legal_id
     * @return true if image exists, false otherwise
     */
    @Override
    public boolean selfieImageExists(String customerId) {
        String selfieFileName = customerId + ".jpg";
        Path selfieFilePath = Paths.get(uploadDir, "selfie", selfieFileName);
        return Files.exists(selfieFilePath);
    }

    private void saveBase64ToFile(String base64, String filePath) throws Exception {
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(decodedBytes);
        }
    }

    private String encodeFileToBase64(String filePath) throws Exception {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        return Base64.getEncoder().encodeToString(fileContent);
    }
}