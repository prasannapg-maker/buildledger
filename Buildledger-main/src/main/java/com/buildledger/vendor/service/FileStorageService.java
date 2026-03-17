package com.buildledger.vendor.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for file storage.
 * Implement this interface for local storage, AWS S3, Azure Blob, GCP Storage, etc.
 */
public interface FileStorageService {

    /**
     * Stores the given file and returns its URI/path.
     */
    String store(MultipartFile file, Long vendorId);

    /**
     * Loads a file as a Spring Resource so it can be served for download.
     */
    Resource load(String fileUri);
}
