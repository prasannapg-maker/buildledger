package com.buildledger.vendor.service.impl;

import com.buildledger.vendor.dto.VendorDocumentResponseDTO;
import com.buildledger.vendor.enums.DocumentType;
import com.buildledger.vendor.enums.VerificationStatus;
import com.buildledger.vendor.model.Vendor;
import com.buildledger.vendor.model.VendorDocument;
import com.buildledger.vendor.repository.VendorDocumentRepository;
import com.buildledger.vendor.repository.VendorRepository;
import com.buildledger.vendor.service.FileStorageService;
import com.buildledger.vendor.service.VendorDocumentService;
import com.buildledger.vendor.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VendorDocumentServiceImpl implements VendorDocumentService {

    private final VendorDocumentRepository documentRepository;
    private final VendorRepository vendorRepository;
    private final FileStorageService fileStorageService;
    private final VendorService vendorService;

    @Autowired
    public VendorDocumentServiceImpl(VendorDocumentRepository documentRepository,
                                     VendorRepository vendorRepository,
                                     FileStorageService fileStorageService,
                                     @Lazy VendorService vendorService) {
        this.documentRepository = documentRepository;
        this.vendorRepository = vendorRepository;
        this.fileStorageService = fileStorageService;
        this.vendorService = vendorService;
    }

    @Override
    public ResponseEntity<VendorDocumentResponseDTO> uploadDocument(Long vendorId,
                                                                    MultipartFile file,
                                                                    String docType) {
        Optional<Vendor> optionalVendor = vendorRepository.findById(vendorId);
        if (optionalVendor.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        DocumentType documentType;
        try {
            documentType = DocumentType.valueOf(docType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String fileUri = fileStorageService.store(file, vendorId);

        VendorDocument document = new VendorDocument();
        document.setVendor(optionalVendor.get());
        document.setDocType(documentType);
        document.setFileUri(fileUri);
        document.setUploadedDate(LocalDate.now());
        document.setVerificationStatus(VerificationStatus.PENDING);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponseDTO(documentRepository.save(document)));
    }

    @Override
    public List<VendorDocumentResponseDTO> getDocumentsByVendorId(Long vendorId) {
        return documentRepository.findByVendorVendorId(vendorId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> updateVerificationStatus(Long documentId, VerificationStatus status) {
        Optional<VendorDocument> optional = documentRepository.findById(documentId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Document not found with ID: " + documentId);
        }

        VendorDocument document = optional.get();
        VerificationStatus current = document.getVerificationStatus();

        if (current == VerificationStatus.APPROVED && status == VerificationStatus.REJECTED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Document is already APPROVED and cannot be rejected. Once approved, verification status cannot be changed.");
        }

        if (current == VerificationStatus.REJECTED && status == VerificationStatus.APPROVED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Document is already REJECTED and cannot be approved. Once rejected, verification status cannot be changed.");
        }

        if (current == status) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Document is already in " + status + " status.");
        }

        document.setVerificationStatus(status);
        documentRepository.save(document);

        vendorService.autoUpdateVendorStatus(document.getVendor().getVendorId());

        return ResponseEntity.ok(mapToResponseDTO(document));
    }

    @Override
    public ResponseEntity<String> deleteDocument(Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found.");
        }
        documentRepository.deleteById(documentId);
        return ResponseEntity.ok("Document deleted successfully.");
    }
    @Override
    public ResponseEntity<Resource> downloadDocument(Long documentId) {
        Optional<VendorDocument> optional = documentRepository.findById(documentId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        VendorDocument document = optional.get();
        Resource resource = fileStorageService.load(document.getFileUri());
        String filename = document.getFileUri().replaceAll(".*[\\\\/]", "");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    private VendorDocumentResponseDTO mapToResponseDTO(VendorDocument document) {
        VendorDocumentResponseDTO dto = new VendorDocumentResponseDTO();
        dto.setDocumentId(document.getDocumentId());
        dto.setDocType(document.getDocType());
        dto.setFileUri(document.getFileUri());
        dto.setUploadedDate(document.getUploadedDate());
        dto.setVerificationStatus(document.getVerificationStatus());
        dto.setVendorId(document.getVendor().getVendorId());
        return dto;
    }
}