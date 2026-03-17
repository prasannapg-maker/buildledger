package com.buildledger.vendor.service.impl;

import com.buildledger.vendor.dto.VendorRequestDTO;
import com.buildledger.vendor.dto.VendorResponseDTO;
import com.buildledger.vendor.enums.VendorStatus;
import com.buildledger.vendor.enums.VerificationStatus;
import com.buildledger.vendor.model.Vendor;
import com.buildledger.vendor.model.VendorDocument;
import com.buildledger.vendor.repository.VendorDocumentRepository;
import com.buildledger.vendor.repository.VendorRepository;
import com.buildledger.vendor.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final VendorDocumentRepository vendorDocumentRepository;

    @Override
    public VendorResponseDTO createVendor(VendorRequestDTO requestDTO) {
        Vendor vendor = new Vendor();
        vendor.setName(requestDTO.getName());
        vendor.setContactInfo(requestDTO.getContactInfo());
        vendor.setCategory(requestDTO.getCategory());
        vendor.setStatus(VendorStatus.PENDING);
        return mapToResponseDTO(vendorRepository.save(vendor));
    }

    @Override
    public List<VendorResponseDTO> getAllVendors() {
        return vendorRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<VendorResponseDTO> getVendorById(Long id) {
        Optional<Vendor> vendor = vendorRepository.findById(id);
        if (vendor.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapToResponseDTO(vendor.get()));
    }

    @Override
    public ResponseEntity<VendorResponseDTO> updateVendor(Long id, VendorRequestDTO requestDTO) {
        Optional<Vendor> optional = vendorRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Vendor vendor = optional.get();
        vendor.setName(requestDTO.getName());
        vendor.setContactInfo(requestDTO.getContactInfo());
        vendor.setCategory(requestDTO.getCategory());
        return ResponseEntity.ok(mapToResponseDTO(vendorRepository.save(vendor)));
    }

    @Override
    public ResponseEntity<String> deleteVendor(Long id) {
        if (!vendorRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vendor not found.");
        }
        vendorRepository.deleteById(id);
        return ResponseEntity.ok("Vendor deleted successfully.");
    }

    @Override
    public List<VendorResponseDTO> getPendingVendors() {
        return vendorRepository.findByStatus(VendorStatus.PENDING)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> acceptVendor(Long id) {
        Optional<Vendor> optional = vendorRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Vendor not found with ID: " + id);
        }
        Vendor vendor = optional.get();
        if (vendor.getStatus() == VendorStatus.VERIFIED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vendor is already VERIFIED and cannot be accepted again.");
        }
        if (vendor.getStatus() == VendorStatus.REJECTED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vendor has been REJECTED and cannot be accepted. Once rejected, the status cannot be changed.");
        }
        vendor.setStatus(VendorStatus.VERIFIED);
        return ResponseEntity.ok(mapToResponseDTO(vendorRepository.save(vendor)));
    }

    @Override
    public ResponseEntity<?> rejectVendor(Long id) {
        Optional<Vendor> optional = vendorRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Vendor not found with ID: " + id);
        }
        Vendor vendor = optional.get();
        if (vendor.getStatus() == VendorStatus.VERIFIED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vendor is already VERIFIED and cannot be rejected. Once verified, the status cannot be changed.");
        }
        if (vendor.getStatus() == VendorStatus.REJECTED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vendor is already REJECTED and cannot be rejected again.");
        }
        vendor.setStatus(VendorStatus.REJECTED);
        return ResponseEntity.ok(mapToResponseDTO(vendorRepository.save(vendor)));
    }

    @Override
    public void autoUpdateVendorStatus(Long vendorId) {
        Optional<Vendor> optional = vendorRepository.findById(vendorId);
        if (optional.isEmpty()) return;

        Vendor vendor = optional.get();

        if (vendor.getStatus() != VendorStatus.PENDING) return;

        List<VendorDocument> documents = vendorDocumentRepository.findByVendorVendorId(vendorId);

        if (documents.isEmpty()) return;

        boolean allApproved = documents.stream()
                .allMatch(d -> d.getVerificationStatus() == VerificationStatus.APPROVED);

        boolean anyRejected = documents.stream()
                .anyMatch(d -> d.getVerificationStatus() == VerificationStatus.REJECTED);

        if (allApproved) {
            vendor.setStatus(VendorStatus.VERIFIED);
            vendorRepository.save(vendor);
        } else if (anyRejected) {
            vendor.setStatus(VendorStatus.REJECTED);
            vendorRepository.save(vendor);
        }
    }

    private VendorResponseDTO mapToResponseDTO(Vendor vendor) {
        VendorResponseDTO dto = new VendorResponseDTO();
        dto.setVendorId(vendor.getVendorId());
        dto.setName(vendor.getName());
        dto.setContactInfo(vendor.getContactInfo());
        dto.setCategory(vendor.getCategory());
        dto.setStatus(vendor.getStatus());
        dto.setCreatedAt(vendor.getCreatedAt());
        dto.setUpdatedAt(vendor.getUpdatedAt());
        return dto;
    }
}