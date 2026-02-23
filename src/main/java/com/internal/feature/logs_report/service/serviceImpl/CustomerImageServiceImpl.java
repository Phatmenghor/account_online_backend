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
import java.util.Comparator;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerImageServiceImpl implements CustomerImageService {

    private final CustomerImageRepository customerImageRepository;

    @Value("${file.upload.directory:/app/customer-image}")
    private String uploadDir;

    @Value("${file.upload.nid:/nid}")
    private String nidPath;

    @Value("${file.upload.selfie:/selfie}")
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

            // Check if images were already uploaded via DocumentUploadController
            // We assume that if nidImage contains "nid_", it's a filename, otherwise base64
            // OR we can add fields to CustomerFileUploadRequestDto.
            // For now, let's stick to the plan: OpenAccountServiceImpl will handle this
            // logic
            // and pass base64 OR we update this to handle filenames.

            // Actually, best approach:
            // If the input string looks like a filename (ends with .jpg/png and has no
            // base64 header), treat as file.
            // But strict Base64 validation is better.

            // Let's rely on OpenAccountServiceImpl to populate this DTO.
            // If we use the new flow, OpenAccountServiceImpl won't even call this method
            // with Base64.
            // It might call it with null Base64 but we need to store the filenames in DB.

            String nidFileName;
            String selfieFileName;

            // 1. Handle NID
            if (request.getNidImage() != null && !request.getNidImage().isEmpty()) {
                // Check if it's already a filename (simple heuristic or flag)
                if (request.getNidImage().startsWith("nid_")) {
                    nidFileName = request.getNidImage();
                    log.info("NID image already uploaded: {}", nidFileName);
                } else {
                    // It is Base64
                    nidFileName = "nid_" + legalId + ".jpg";
                    String nidFullPath = Paths.get(uploadDir, "nid", nidFileName).toString();
                    saveBase64ToFile(request.getNidImage(), nidFullPath);
                }
            } else {
                // Check if file already exists on disk (fallback) - find the actual filename
                Path nidDir = Paths.get(uploadDir, "nid");
                Path existingNid = findLatestFile(nidDir, "nid_" + legalId + "_");
                if (existingNid != null) {
                    nidFileName = existingNid.getFileName().toString();
                    log.info("NID image data missing but file found on disk: {}", nidFileName);
                } else {
                    log.warn("NID image data is missing and file not found - skipping save");
                    nidFileName = null;
                }
            }

            // 2. Handle Selfie
            if (request.getSelfieImage() != null && !request.getSelfieImage().isEmpty()) {
                if (request.getSelfieImage().startsWith("selfie_")) {
                    selfieFileName = request.getSelfieImage();
                    log.info("Selfie image already uploaded: {}", selfieFileName);
                } else {
                    selfieFileName = "selfie_" + legalId + ".jpg";
                    String selfieFullPath = Paths.get(uploadDir, "selfie", selfieFileName).toString();
                    saveBase64ToFile(request.getSelfieImage(), selfieFullPath);
                }
            } else {
                // Check if file already exists on disk (fallback) - find the actual filename
                Path selfieDir = Paths.get(uploadDir, "selfie");
                Path existingSelfie = findLatestFile(selfieDir, "selfie_" + legalId + "_");
                if (existingSelfie != null) {
                    selfieFileName = existingSelfie.getFileName().toString();
                    log.info("Selfie image data missing but file found on disk: {}", selfieFileName);
                } else {
                    log.warn("Selfie image data is missing and file not found - skipping save");
                    selfieFileName = null;
                }
            }

            // Only save to DB if filenames are present
            if (nidFileName != null) {
                customerImageRepository.save(CustomerImage.builder()
                        .type("NID")
                        .name(nidFileName)
                        .filePath(nidFileName)
                        .build());
            }

            if (selfieFileName != null) {
                customerImageRepository.save(CustomerImage.builder()
                        .type("SELFIE")
                        .name(selfieFileName)
                        .filePath(selfieFileName)
                        .build());
            }

            log.info("Saved customer images metadata: NID={}, Selfie={}", nidFileName, selfieFileName);

            return CustomerImageUploadResponseDto.builder()
                    .nidImagePath(nidFileName)
                    .selfieImagePath(selfieFileName)
                    .build();

        } catch (Exception e) {
            log.error("Failed to save customer images: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving images", e);
        }
    }

    @Override
    public String saveBase64File(String base64, String filename, String type) throws Exception {
        String subFolder = "selfie".equalsIgnoreCase(type) ? "selfie" : "nid";
        String filePath = Paths.get(uploadDir, subFolder, filename).toString();

        new File(Paths.get(uploadDir, subFolder).toString()).mkdirs();
        saveBase64ToFile(base64, filePath);

        log.info("Saved base64 file: {} to {}", filename, filePath);
        return filename;
    }

    /**
     * Save uploaded file directly to disk.
     * returns the filename.
     */
    @Override
    public String saveUploadedFile(org.springframework.web.multipart.MultipartFile file, String filename)
            throws Exception {
        // Determine sub-folder based on filename prefix (nid_ or selfie_)
        String subFolder = filename.startsWith("nid_") ? "nid" : "selfie";
        String folderPath = Paths.get(uploadDir, subFolder).toString();

        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Path targetPath = Paths.get(folderPath, filename);
        try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
            fos.write(file.getBytes());
        }

        log.info("Saved uploaded file: {} to {}", filename, targetPath);
        return filename;
    }

    /** Get NID image file as Resource (for email attachment) */
    @Override
    public Resource getNidImageResourceForEmail(String customerId) {
        try {
            Path dir = Paths.get(uploadDir, "nid");
            Path imagePath = findLatestFile(dir, "nid_" + customerId + "_");
            if (imagePath == null) {
                log.warn("NID image not found for customer: {}", customerId);
                return null;
            }
            log.info("Retrieved NID image for email: {}", imagePath);
            return new FileSystemResource(imagePath.toFile());

        } catch (Exception e) {
            log.error("Failed to get NID image resource: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Get NID image as byte array */
    @Override
    public byte[] getNidImageBytes(String customerId) {
        try {
            Path dir = Paths.get(uploadDir, "nid");
            Path imagePath = findLatestFile(dir, "nid_" + customerId + "_");
            if (imagePath == null) {
                log.warn("NID image not found for customer: {} in dir: {}", customerId, dir.toAbsolutePath());
                return null;
            }

            byte[] bytes = Files.readAllBytes(imagePath);
            log.info("Retrieved NID image bytes for customer: {} ({} bytes) from {}", customerId, bytes.length, imagePath.getFileName());
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
            Path dir = Paths.get(uploadDir, "selfie");
            Path imagePath = findLatestFile(dir, "selfie_" + customerId + "_");
            if (imagePath == null) {
                log.warn("Selfie image not found for customer: {}", customerId);
                return null;
            }

            log.info("Retrieved Selfie image for email: {}", imagePath);
            return new FileSystemResource(imagePath.toFile());

        } catch (Exception e) {
            log.error("Failed to get Selfie image resource: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Get Selfie image as byte array */
    @Override
    public byte[] getSelfieImageBytes(String customerId) {
        try {
            Path dir = Paths.get(uploadDir, "selfie");
            Path imagePath = findLatestFile(dir, "selfie_" + customerId + "_");
            if (imagePath == null) {
                log.warn("Selfie image not found for customer: {} in dir: {}", customerId, dir.toAbsolutePath());
                return null;
            }

            byte[] bytes = Files.readAllBytes(imagePath);
            log.info("Retrieved Selfie image bytes for customer: {} ({} bytes) from {}", customerId, bytes.length, imagePath.getFileName());
            return bytes;

        } catch (IOException e) {
            log.error("Failed to read Selfie image bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Check if any NID image exists for the customer (prefix match) */
    @Override
    public boolean nidImageExists(String customerId) {
        Path dir = Paths.get(uploadDir, "nid");
        String prefix = "nid_" + customerId + "_";
        try (Stream<Path> files = Files.list(dir)) {
            return files.anyMatch(p -> p.getFileName().toString().startsWith(prefix));
        } catch (IOException e) {
            return false;
        }
    }

    /** Check if any Selfie image exists for the customer (prefix match) */
    @Override
    public boolean selfieImageExists(String customerId) {
        Path dir = Paths.get(uploadDir, "selfie");
        String prefix = "selfie_" + customerId + "_";
        try (Stream<Path> files = Files.list(dir)) {
            return files.anyMatch(p -> p.getFileName().toString().startsWith(prefix));
        } catch (IOException e) {
            return false;
        }
    }

    /** Utility: Find the most recently modified file in a directory whose name starts with the given prefix */
    private Path findLatestFile(Path dir, String prefix) {
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(p -> p.getFileName().toString().startsWith(prefix))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                    .orElse(null);
        } catch (IOException e) {
            log.warn("Could not scan directory {} for prefix {}: {}", dir, prefix, e.getMessage());
            return null;
        }
    }

    /** Utility: Save base64 image to file */
    private void saveBase64ToFile(String base64, String filePath) throws Exception {
        if (base64 == null || base64.isEmpty())
            return;

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

        // 2. Sanitize: Remove all characters not in the Base64 alphabet (A-Z, a-z, 0-9,
        // +, /, =)
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
