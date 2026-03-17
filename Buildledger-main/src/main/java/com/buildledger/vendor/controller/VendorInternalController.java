package com.buildledger.vendor.controller;

import com.buildledger.vendor.dto.VendorDocumentResponseDTO;
import com.buildledger.vendor.dto.VendorResponseDTO;
import com.buildledger.vendor.enums.VendorStatus;
import com.buildledger.vendor.service.VendorDocumentService;
import com.buildledger.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor Internal APIs", description = "Internal endpoints for use by other microservices (Contracts, Finance, Compliance)")
public class VendorInternalController {

    private final VendorService vendorService;
    private final VendorDocumentService documentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get vendor details by ID (internal)")
    public ResponseEntity<VendorResponseDTO> getVendorById(@PathVariable Long id) {
        return vendorService.getVendorById(id);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get only the vendor status (internal)")
    public ResponseEntity<VendorStatus> getVendorStatus(@PathVariable Long id) {
        ResponseEntity<VendorResponseDTO> response = vendorService.getVendorById(id);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return ResponseEntity.status(response.getStatusCode()).build();
        }
        return ResponseEntity.ok(response.getBody().getStatus());
    }

    @GetMapping("/{vendorId}/documents")
    @Operation(summary = "Get all documents for a vendor (internal)")
    public ResponseEntity<List<VendorDocumentResponseDTO>> getVendorDocuments(
            @PathVariable Long vendorId) {
        return ResponseEntity.ok(documentService.getDocumentsByVendorId(vendorId));
    }
}
