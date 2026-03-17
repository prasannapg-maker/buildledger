package com.buildledger.vendor.service;

import com.buildledger.vendor.dto.VendorDocumentResponseDTO;
import com.buildledger.vendor.enums.VerificationStatus;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VendorDocumentService {

    ResponseEntity<VendorDocumentResponseDTO> uploadDocument(Long vendorId, MultipartFile file, String docType);

    List<VendorDocumentResponseDTO> getDocumentsByVendorId(Long vendorId);

    ResponseEntity<?> updateVerificationStatus(Long documentId, VerificationStatus status);

    ResponseEntity<String> deleteDocument(Long documentId);

    ResponseEntity<Resource> downloadDocument(Long documentId);
}