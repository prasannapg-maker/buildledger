package com.buildledger.vendor.service.impl;

import com.buildledger.vendor.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.file-storage.base-path:uploads}")
    private String basePath;

    @Override
    public String store(MultipartFile file, Long vendorId) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path vendorDir = Paths.get(basePath, "vendor_" + vendorId);
            Files.createDirectories(vendorDir);
            Path targetPath = vendorDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource load(String fileUri) {
        try {
            Path filePath = Paths.get(fileUri);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found or not readable: " + fileUri);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file: " + e.getMessage(), e);
        }
    }
}
