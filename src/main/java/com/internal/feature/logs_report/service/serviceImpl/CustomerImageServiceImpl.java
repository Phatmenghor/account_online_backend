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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.Base64;
import java.util.Comparator;
import java.util.Optional;
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

    // ─────────────────────────────────────────────
    // Week folder: 2026-03-W11
    // ─────────────────────────────────────────────

    /**
     * Returns current week folder name.
     * Format: yyyy-MM-W{isoWeek}
     * Example: 2026-03-W11
     */
    private String getCurrentWeekFolder() {
        LocalDate today = LocalDate.now();
        int isoWeek = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String monthPart = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return String.format("%s-W%02d", monthPart, isoWeek);
    }

    /**
     * Resolves and creates the week sub-folder for a given type.
     * Example: /app/customer-image/nid/2026-03-W11/
     */
    private Path resolveWeekFolder(String subFolder) {
        Path dir = Paths.get(uploadDir, subFolder, getCurrentWeekFolder());
        dir.toFile().mkdirs();
        log.debug("Resolved week folder: {}", dir);
        return dir;
    }

    // ─────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────

    @Override
    public CustomerImageUploadResponseDto saveCustomerImages(CustomerFileUploadRequestDto request) {
        try {
            new File(uploadDir + nidPath).mkdirs();
            new File(uploadDir + selfiePath).mkdirs();

            String legalId = request.getLegal_id();
            String nidFileName;
            String selfieFileName;

            // ── Handle NID ──
            if (request.getNidImage() != null && !request.getNidImage().isEmpty()) {
                if (request.getNidImage().startsWith("nid_")) {
                    nidFileName = request.getNidImage();
                    log.info("NID image already uploaded: {}", nidFileName);
                } else {
                    nidFileName = "nid_" + legalId + ".jpg";
                    Path weekDir = resolveWeekFolder("nid");
                    saveBase64ToFile(request.getNidImage(), weekDir.resolve(nidFileName).toString());
                }
            } else {
                Path existing = findLatestFileRecursive(
                        Paths.get(uploadDir, "nid"), "nid_" + legalId + "_");
                if (existing != null) {
                    nidFileName = existing.getFileName().toString();
                    log.info("NID image data missing but file found on disk: {}", nidFileName);
                } else {
                    log.warn("NID image data is missing and file not found - skipping save");
                    nidFileName = null;
                }
            }

            // ── Handle Selfie ──
            if (request.getSelfieImage() != null && !request.getSelfieImage().isEmpty()) {
                if (request.getSelfieImage().startsWith("selfie_")) {
                    selfieFileName = request.getSelfieImage();
                    log.info("Selfie image already uploaded: {}", selfieFileName);
                } else {
                    selfieFileName = "selfie_" + legalId + ".jpg";
                    Path weekDir = resolveWeekFolder("selfie");
                    saveBase64ToFile(request.getSelfieImage(), weekDir.resolve(selfieFileName).toString());
                }
            } else {
                Path existing = findLatestFileRecursive(
                        Paths.get(uploadDir, "selfie"), "selfie_" + legalId + "_");
                if (existing != null) {
                    selfieFileName = existing.getFileName().toString();
                    log.info("Selfie image data missing but file found on disk: {}", selfieFileName);
                } else {
                    log.warn("Selfie image data is missing and file not found - skipping save");
                    selfieFileName = null;
                }
            }

            // ── Persist to DB ──
            if (nidFileName != null) {
                customerImageRepository.save(CustomerImage.builder()
                        .type("NID")
                        .legal_id(legalId)
                        .name(nidFileName)
                        .filePath(getCurrentWeekFolder() + "/" + nidFileName)
                        .build());
            }

            if (selfieFileName != null) {
                customerImageRepository.save(CustomerImage.builder()
                        .type("SELFIE")
                        .legal_id(legalId)
                        .name(selfieFileName)
                        .filePath(getCurrentWeekFolder() + "/" + selfieFileName)
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

    /**
     * Save a raw base64 string into the current week folder.
     * Called by DocumentUploadController for JSON base64 uploads.
     * Filename already contains legalId + timestamp + random from buildFilename().
     */
    @Override
    public String saveBase64File(String base64, String filename, String type) throws Exception {
        String subFolder = "selfie".equalsIgnoreCase(type) ? "selfie" : "nid";
        Path weekDir = resolveWeekFolder(subFolder);
        String filePath = weekDir.resolve(filename).toString();

        saveBase64ToFile(base64, filePath);

        String legalId = extractLegalIdFromFilename(filename);

        customerImageRepository.save(CustomerImage.builder()
                .type(subFolder.toUpperCase())
                .legal_id(legalId)
                .name(filename)
                .filePath(getCurrentWeekFolder() + "/" + filename)
                .build());

        log.info("Saved base64 file: {} → week folder: {}", filename, weekDir);
        return filename;
    }

    /**
     * Save a multipart file into the current week folder.
     * Called by DocumentUploadController for multipart uploads.
     */
    @Override
    public String saveUploadedFile(MultipartFile file, String filename) throws Exception {
        String subFolder = filename.startsWith("selfie_") ? "selfie" : "nid";
        Path weekDir = resolveWeekFolder(subFolder);
        Path targetPath = weekDir.resolve(filename);

        try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
            fos.write(file.getBytes());
        }

        String legalId = extractLegalIdFromFilename(filename);

        customerImageRepository.save(CustomerImage.builder()
                .type(subFolder.toUpperCase())
                .legal_id(legalId)
                .name(filename)
                .filePath(getCurrentWeekFolder() + "/" + filename)
                .build());

        log.info("Saved uploaded file: {} → week folder: {}", filename, weekDir);
        return filename;
    }

    @Override
    public Resource getNidImageResourceForEmail(String customerId) {
        try {
            Path imagePath = findLatestFileRecursive(
                    Paths.get(uploadDir, "nid"), "nid_" + customerId + "_");
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

    @Override
    public byte[] getNidImageBytes(String customerId) {
        try {
            Path imagePath = findLatestFileRecursive(
                    Paths.get(uploadDir, "nid"), "nid_" + customerId + "_");
            if (imagePath == null) {
                log.warn("NID image not found for customer: {} in dir: {}",
                        customerId, Paths.get(uploadDir, "nid").toAbsolutePath());
                return null;
            }
            byte[] bytes = Files.readAllBytes(imagePath);
            log.info("Retrieved NID image bytes for customer: {} ({} bytes) from {}",
                    customerId, bytes.length, imagePath.getFileName());
            return bytes;
        } catch (IOException e) {
            log.error("Failed to read NID image bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Resource getSelfieImageResourceForEmail(String customerId) {
        try {
            Path imagePath = findLatestFileRecursive(
                    Paths.get(uploadDir, "selfie"), "selfie_" + customerId + "_");
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

    @Override
    public byte[] getSelfieImageBytes(String customerId) {
        try {
            Path imagePath = findLatestFileRecursive(
                    Paths.get(uploadDir, "selfie"), "selfie_" + customerId + "_");
            if (imagePath == null) {
                log.warn("Selfie image not found for customer: {} in dir: {}",
                        customerId, Paths.get(uploadDir, "selfie").toAbsolutePath());
                return null;
            }
            byte[] bytes = Files.readAllBytes(imagePath);
            log.info("Retrieved Selfie image bytes for customer: {} ({} bytes) from {}",
                    customerId, bytes.length, imagePath.getFileName());
            return bytes;
        } catch (IOException e) {
            log.error("Failed to read Selfie image bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean nidImageExists(String customerId) {
        return findLatestFileRecursive(
                Paths.get(uploadDir, "nid"), "nid_" + customerId + "_") != null;
    }

    @Override
    public boolean selfieImageExists(String customerId) {
        return findLatestFileRecursive(
                Paths.get(uploadDir, "selfie"), "selfie_" + customerId + "_") != null;
    }

    /**
     * Find file by exact filename — used by CustomerImageFileController.
     * Searches flat (old) + week subfolders (new) at depth 2.
     */
    public Optional<Path> findFileByName(String subFolder, String filename) {
        Path baseDir = Paths.get(uploadDir, subFolder);
        try (Stream<Path> walk = Files.walk(baseDir, 2)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(filename))
                    .findFirst();
        } catch (IOException e) {
            log.warn("Could not search for file {} in {}: {}", filename, baseDir, e.getMessage());
            return Optional.empty();
        }
    }

    // ─────────────────────────────────────────────
    // Private utilities
    // ─────────────────────────────────────────────

    /**
     * Search recursively through base dir AND all week sub-folders.
     * Handles both old flat structure and new week structure.
     * Depth 2: base → week folder → file
     */
    private Path findLatestFileRecursive(Path baseDir, String prefix) {
        try (Stream<Path> walk = Files.walk(baseDir, 2)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(prefix))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                    .orElse(null);
        } catch (IOException e) {
            log.warn("Could not scan directory {} for prefix {}: {}", baseDir, prefix, e.getMessage());
            return null;
        }
    }

    /**
     * Extract legalId from filename built by DocumentUploadController.buildFilename().
     * Pattern: {type}_{legalId}_{timestamp}_{random}.jpg
     * Example: nid_250319613_20260313043535123_a3f9c1.jpg → 250319613
     */
    private String extractLegalIdFromFilename(String filename) {
        try {
            String withoutExt = filename.contains(".")
                    ? filename.substring(0, filename.lastIndexOf('.'))
                    : filename;
            // parts[0]=type, parts[1]=legalId, parts[2]=timestamp_random
            String[] parts = withoutExt.split("_", 3);
            if (parts.length >= 2) {
                return parts[1];
            }
        } catch (Exception e) {
            log.warn("Could not extract legalId from filename: {}", filename);
        }
        return null;
    }

    /**
     * Decode and write a base64 string to a file path.
     * Strips data URI prefix and sanitizes non-Base64 characters.
     */
    private void saveBase64ToFile(String base64, String filePath) throws Exception {
        if (base64 == null || base64.isEmpty()) return;

        if (base64.contains(",")) {
            int idx = base64.indexOf("base64,");
            base64 = (idx != -1)
                    ? base64.substring(idx + 7)
                    : base64.substring(base64.lastIndexOf(",") + 1);
        }

        base64 = base64.replaceAll("[^A-Za-z0-9+/=]", "");

        byte[] decoded = Base64.getDecoder().decode(base64);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(decoded);
        }
    }

    /** Encode file to Base64 string (utility, optional use) */
    private String encodeFileToBase64(String filePath) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return Base64.getEncoder().encodeToString(bytes);
    }
}