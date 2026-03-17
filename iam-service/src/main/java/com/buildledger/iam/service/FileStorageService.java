package com.buildledger.iam.service;

import com.buildledger.iam.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.storage.base-path:./uploads/vendor-documents}")
    private String basePath;

    @Value("${file.storage.allowed-extensions:pdf,jpg,jpeg,png,doc,docx}")
    private String allowedExtensionsConfig;

    private Path storageRoot;
    private List<String> allowedExtensions;

    @PostConstruct
    public void init() {
        storageRoot = Paths.get(basePath).toAbsolutePath().normalize();
        allowedExtensions = Arrays.asList(allowedExtensionsConfig.toLowerCase().split(","));
        try {
            Files.createDirectories(storageRoot);
            log.info("File storage initialized at: {}", storageRoot);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize file storage at: " + storageRoot, e);
        }
    }

    /**
     * Store a multipart file under a vendor-specific subfolder.
     *
     * @return The relative URL path for database storage.
     */
    public String storeFile(MultipartFile file, Long vendorId, String documentType) {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        );
        String extension = getExtension(originalFileName).toLowerCase();

        if (!allowedExtensions.contains(extension)) {
            throw new FileStorageException("File type '" + extension + "' is not allowed. " +
                    "Allowed types: " + allowedExtensionsConfig);
        }

        // Create vendor-specific directory
        Path vendorDir = storageRoot.resolve("vendor-" + vendorId).normalize();
        try {
            Files.createDirectories(vendorDir);
        } catch (IOException e) {
            throw new FileStorageException("Could not create vendor directory", e);
        }

        // Generate unique filename
        String uniqueFileName = documentType.toLowerCase().replace(" ", "_")
                + "_" + UUID.randomUUID().toString().substring(0, 8)
                + "." + extension;

        Path targetPath = vendorDir.resolve(uniqueFileName).normalize();

        // Security: ensure the target is within base directory
        if (!targetPath.startsWith(storageRoot)) {
            throw new FileStorageException("Cannot store file outside designated storage directory");
        }

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {} for vendorId={}", uniqueFileName, vendorId);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFileName, e);
        }

        // Return relative URL
        return "/uploads/vendor-documents/vendor-" + vendorId + "/" + uniqueFileName;
    }

    /**
     * Delete a file by its stored URL path.
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String relativePath = fileUrl.replaceFirst("/uploads/vendor-documents/", "");
            Path filePath = storageRoot.resolve(relativePath).normalize();

            if (!filePath.startsWith(storageRoot)) {
                log.warn("Attempt to delete file outside storage root: {}", fileUrl);
                return;
            }

            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file");
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10 MB
            throw new FileStorageException("File size exceeds maximum limit of 10MB");
        }
    }

    public String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new FileStorageException("File must have a valid extension");
        }
        return filename.substring(dotIndex + 1);
    }
}
