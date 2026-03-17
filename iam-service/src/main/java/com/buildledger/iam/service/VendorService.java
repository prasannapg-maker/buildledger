package com.buildledger.iam.service;

import com.buildledger.iam.dto.request.VendorRegistrationRequest;
import com.buildledger.iam.dto.response.VendorDocumentResponse;
import com.buildledger.iam.dto.response.VendorResponse;
import com.buildledger.iam.entity.*;
import com.buildledger.iam.exception.DuplicateResourceException;
import com.buildledger.iam.exception.InvalidRequestException;
import com.buildledger.iam.exception.ResourceNotFoundException;
import com.buildledger.iam.repository.VendorDocumentRepository;
import com.buildledger.iam.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorDocumentRepository documentRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final AuditService auditService;

    // Injected circularly via setter to avoid cycle – use constructor injection on UserService which doesn't depend on VendorService
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Vendor self-registration.
     */
    @Transactional
    public VendorResponse registerVendor(VendorRegistrationRequest request, String ipAddress) {
        if (vendorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Vendor with email '" + request.getEmail() + "' already exists");
        }
        if (request.getGstNumber() != null && vendorRepository.existsByGstNumber(request.getGstNumber())) {
            throw new DuplicateResourceException("Vendor with GST number '" + request.getGstNumber() + "' already exists");
        }

        Vendor vendor = Vendor.builder()
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .category(request.getCategory())
                .gstNumber(request.getGstNumber())
                .panNumber(request.getPanNumber())
                .status(VendorStatus.PENDING_VERIFICATION)
                .build();

        vendor = vendorRepository.save(vendor);

        auditService.logSuccess(null, "VENDOR_REGISTERED", "vendors/" + vendor.getId(), ipAddress);
        log.info("Vendor registered: id={}, email={}", vendor.getId(), vendor.getEmail());

        return VendorResponse.from(vendor);
    }

    /**
     * Upload a document for a vendor.
     */
    @Transactional
    public VendorDocumentResponse uploadDocument(Long vendorId, MultipartFile file,
                                                  String documentType, String ipAddress) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> ResourceNotFoundException.vendor(vendorId));

        if (vendor.getStatus() == VendorStatus.REJECTED || vendor.getStatus() == VendorStatus.SUSPENDED) {
            throw new InvalidRequestException("Cannot upload documents for a " + vendor.getStatus() + " vendor");
        }

        String fileUrl = fileStorageService.storeFile(file, vendorId, documentType);

        VendorDocument document = VendorDocument.builder()
                .vendor(vendor)
                .documentType(documentType)
                .originalFileName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .fileExtension(fileStorageService.getExtension(
                        file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"))
                .fileSizeBytes(file.getSize())
                .status(DocumentStatus.PENDING)
                .build();

        document = documentRepository.save(document);

        auditService.logSuccess(null, "DOCUMENT_UPLOADED",
                "vendors/" + vendorId + "/documents/" + document.getId(), ipAddress);

        return VendorDocumentResponse.from(document);
    }

    /**
     * Get vendor status.
     */
    @Transactional(readOnly = true)
    public VendorResponse getVendorStatus(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .map(VendorResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.vendor(vendorId));
    }

    /**
     * Get vendor by ID.
     */
    @Transactional(readOnly = true)
    public VendorResponse getVendorById(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .map(VendorResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.vendor(vendorId));
    }

    /**
     * Get all documents for a vendor.
     */
    @Transactional(readOnly = true)
    public List<VendorDocumentResponse> getVendorDocuments(Long vendorId) {
        if (!vendorRepository.existsById(vendorId)) {
            throw ResourceNotFoundException.vendor(vendorId);
        }
        return documentRepository.findByVendorId(vendorId).stream()
                .map(VendorDocumentResponse::from)
                .toList();
    }

    /**
     * List vendors with optional filters (admin).
     */
    @Transactional(readOnly = true)
    public Page<VendorResponse> getAllVendors(String search, VendorStatus status, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return vendorRepository.searchVendors(search.trim(), pageable).map(VendorResponse::from);
        }
        if (status != null) {
            return vendorRepository.findByStatus(status, pageable).map(VendorResponse::from);
        }
        return vendorRepository.findAll(pageable).map(VendorResponse::from);
    }

    /**
     * Admin approves a vendor.
     */
    @Transactional
    public VendorResponse approveVendor(Long vendorId, String approvedByEmail, String ipAddress) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> ResourceNotFoundException.vendor(vendorId));

        vendor.setStatus(VendorStatus.APPROVED);
        vendor.setApprovedAt(LocalDateTime.now());
        vendor.setApprovedBy(approvedByEmail);
        vendor.setRejectionReason(null);
        vendor = vendorRepository.save(vendor);

        emailService.sendVendorApprovalEmail(vendor.getEmail(), vendor.getCompanyName(), "APPROVED", null);
        auditService.logSuccess(null, "VENDOR_APPROVED", "vendors/" + vendorId, ipAddress);
        return VendorResponse.from(vendor);
    }

    /**
     * Admin rejects a vendor.
     */
    @Transactional
    public VendorResponse rejectVendor(Long vendorId, String reason,
                                        String rejectedByEmail, String ipAddress) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> ResourceNotFoundException.vendor(vendorId));

        vendor.setStatus(VendorStatus.REJECTED);
        vendor.setRejectionReason(reason);
        vendor = vendorRepository.save(vendor);

        emailService.sendVendorApprovalEmail(vendor.getEmail(), vendor.getCompanyName(), "REJECTED", reason);
        auditService.logSuccess(null, "VENDOR_REJECTED", "vendors/" + vendorId, ipAddress);
        return VendorResponse.from(vendor);
    }

    /**
     * Verify a document (admin).
     */
    @Transactional
    public VendorDocumentResponse verifyDocument(Long documentId, DocumentStatus newStatus,
                                                   String reason, String verifiedBy, String ipAddress) {
        VendorDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> ResourceNotFoundException.document(documentId));

        doc.setStatus(newStatus);
        doc.setVerifiedAt(LocalDateTime.now());
        doc.setVerifiedBy(verifiedBy);
        if (DocumentStatus.REJECTED.equals(newStatus)) {
            doc.setRejectionReason(reason);
        }

        doc = documentRepository.save(doc);
        auditService.logSuccess(null, "DOCUMENT_" + newStatus,
                "documents/" + documentId, ipAddress);

        return VendorDocumentResponse.from(doc);
    }
}
