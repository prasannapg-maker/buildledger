package com.buildledger.iam.controller;

import com.buildledger.iam.dto.request.PanVerificationRequest;
import com.buildledger.iam.dto.request.VendorRegistrationRequest;
import com.buildledger.iam.dto.response.*;
import com.buildledger.iam.entity.DocumentStatus;
import com.buildledger.iam.entity.VendorStatus;
import com.buildledger.iam.security.UserPrincipal;
import com.buildledger.iam.service.ExternalVerificationService;
import com.buildledger.iam.service.VendorService;
import com.buildledger.iam.util.IpAddressUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor", description = "Vendor self-registration, document upload, and verification")
public class VendorController {

    private final VendorService vendorService;
    private final ExternalVerificationService verificationService;

    // ────────────────────────────────────────────────────────
    //  Public: Vendor self-registration
    // ────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Vendor self-registration (public)",
               description = "External vendor registers their company. Status is set to PENDING_VERIFICATION.")
    public ResponseEntity<ApiResponse<VendorResponse>> registerVendor(
            @Valid @RequestBody VendorRegistrationRequest request,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        VendorResponse vendor = vendorService.registerVendor(request, ip);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor registered successfully. Pending verification.", vendor));
    }

    // ────────────────────────────────────────────────────────
    //  Authenticated: Document upload
    // ────────────────────────────────────────────────────────

    @PostMapping(value = "/{vendorId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document for a vendor",
               description = "Supports: GST Certificate, PAN Card, Company Registration, Bank Details, etc. Max size: 10MB.",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<VendorDocumentResponse>> uploadDocument(
            @PathVariable Long vendorId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        VendorDocumentResponse doc = vendorService.uploadDocument(vendorId, file, documentType, ip);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully. Pending verification.", doc));
    }

    // ────────────────────────────────────────────────────────
    //  Authenticated: Vendor status
    // ────────────────────────────────────────────────────────

    @GetMapping("/{vendorId}/status")
    @Operation(summary = "Get vendor status",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVendorStatus(
            @PathVariable Long vendorId) {

        VendorResponse vendor = vendorService.getVendorStatus(vendorId);
        Map<String, Object> statusInfo = Map.of(
                "vendorId", vendor.getId(),
                "companyName", vendor.getCompanyName(),
                "status", vendor.getStatus(),
                "gstVerified", vendor.getGstVerified(),
                "panVerified", vendor.getPanVerified()
        );
        return ResponseEntity.ok(ApiResponse.success(statusInfo));
    }

    @GetMapping("/{vendorId}/documents")
    @Operation(summary = "Get all documents for a vendor",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<VendorDocumentResponse>>> getVendorDocuments(
            @PathVariable Long vendorId) {

        return ResponseEntity.ok(ApiResponse.success(vendorService.getVendorDocuments(vendorId)));
    }

    // ────────────────────────────────────────────────────────
    //  Admin: Vendor management
    // ────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all vendors (admin)",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Page<VendorResponse>>> getAllVendors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) VendorStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return ResponseEntity.ok(ApiResponse.success(
                vendorService.getAllVendors(search, status, PageRequest.of(page, size, sort))));
    }

    @PatchMapping("/{vendorId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a vendor registration (admin)",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<VendorResponse>> approveVendor(
            @PathVariable Long vendorId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        VendorResponse vendor = vendorService.approveVendor(vendorId, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("Vendor approved", vendor));
    }

    @PatchMapping("/{vendorId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a vendor registration (admin)",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<VendorResponse>> rejectVendor(
            @PathVariable Long vendorId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        VendorResponse vendor = vendorService.rejectVendor(vendorId, reason, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("Vendor rejected", vendor));
    }

    @PatchMapping("/documents/{documentId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify or reject a vendor document (admin)",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<VendorDocumentResponse>> verifyDocument(
            @PathVariable Long documentId,
            @RequestParam DocumentStatus status,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        VendorDocumentResponse doc = vendorService.verifyDocument(
                documentId, status, reason, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("Document status updated to " + status, doc));
    }

    // ────────────────────────────────────────────────────────
    //  Admin: External verification APIs
    // ────────────────────────────────────────────────────────

    @GetMapping("/verify-gst/{gstNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify GST number via external API",
               description = "Calls the government GST API. Falls back to manual review on API failure.",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyGst(
            @PathVariable String gstNumber) {

        VerificationResponse result = verificationService.verifyGst(gstNumber);
        return ResponseEntity.ok(ApiResponse.success("GST verification result", result));
    }

    @PostMapping("/verify-pan")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify PAN via external API",
               description = "Calls the PAN verification API. Falls back to manual review on failure.",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyPan(
            @Valid @RequestBody PanVerificationRequest request) {

        VerificationResponse result = verificationService.verifyPan(request.getPanNumber(), request.getNameOnPan());
        return ResponseEntity.ok(ApiResponse.success("PAN verification result", result));
    }

    @PostMapping(value = "/ocr-verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Perform OCR verification on a document",
               description = "Sends document to external OCR API. Falls back to manual review on failure.",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<VerificationResponse>> ocrVerify(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {

        VerificationResponse result = verificationService.performOcrVerification(file, documentType);
        return ResponseEntity.ok(ApiResponse.success("OCR verification result", result));
    }
}
