package com.buildledger.vendor.controller;

import com.buildledger.vendor.dto.VendorDocumentResponseDTO;
import com.buildledger.vendor.enums.VerificationStatus;
import com.buildledger.vendor.service.VendorDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-documents")
@RequiredArgsConstructor
@Tag(name = "Vendor Documents", description = "APIs for managing vendor compliance documents")
public class VendorDocumentController {

    private final VendorDocumentService documentService;

    @PostMapping(value = "/upload/{vendorId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a compliance document for a vendor (PDF only)")
    public ResponseEntity<VendorDocumentResponseDTO> uploadDocument(
            @PathVariable Long vendorId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") String docType) {
        return documentService.uploadDocument(vendorId, file, docType);
    }

    @GetMapping("/vendor/{vendorId}")
    @Operation(summary = "Get all documents for a vendor")
    public ResponseEntity<List<VendorDocumentResponseDTO>> getDocumentsByVendor(
            @PathVariable Long vendorId) {
        return ResponseEntity.ok(documentService.getDocumentsByVendorId(vendorId));
    }

    @PutMapping("/{documentId}/verify")
    @Operation(summary = "Update document verification status")
    public ResponseEntity<?> updateVerificationStatus(
            @PathVariable Long documentId,
            @RequestParam VerificationStatus status) {
        return documentService.updateVerificationStatus(documentId, status);
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete a vendor document")
    public ResponseEntity<String> deleteDocument(@PathVariable Long documentId) {
        return documentService.deleteDocument(documentId);
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "Download a vendor document by ID")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        return documentService.downloadDocument(documentId);
    }
}
