package com.buildledger.vendor.controller;

import com.buildledger.vendor.dto.VendorRequestDTO;
import com.buildledger.vendor.dto.VendorResponseDTO;
import com.buildledger.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor Management", description = "APIs for vendor onboarding and profile management")
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    @Operation(summary = "Create a new vendor")
    public ResponseEntity<VendorResponseDTO> createVendor(@RequestBody VendorRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.createVendor(requestDTO));
    }

    @GetMapping
    @Operation(summary = "Get all vendors")
    public ResponseEntity<List<VendorResponseDTO>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vendor by ID")
    public ResponseEntity<VendorResponseDTO> getVendorById(@PathVariable Long id) {
        return vendorService.getVendorById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vendor details")
    public ResponseEntity<VendorResponseDTO> updateVendor(@PathVariable Long id,
                                                           @RequestBody VendorRequestDTO requestDTO) {
        return vendorService.updateVendor(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vendor")
    public ResponseEntity<String> deleteVendor(@PathVariable Long id) {
        return vendorService.deleteVendor(id);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all vendors with PENDING status")
    public ResponseEntity<List<VendorResponseDTO>> getPendingVendors() {
        return ResponseEntity.ok(vendorService.getPendingVendors());
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Accept a vendor (status → VERIFIED)")
    public ResponseEntity<?> acceptVendor(@PathVariable Long id) {
        return vendorService.acceptVendor(id);
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a vendor (status → REJECTED)")
    public ResponseEntity<?> rejectVendor(@PathVariable Long id) {
        return vendorService.rejectVendor(id);
    }
}
