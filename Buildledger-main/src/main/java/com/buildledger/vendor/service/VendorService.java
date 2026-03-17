package com.buildledger.vendor.service;

import com.buildledger.vendor.dto.VendorRequestDTO;
import com.buildledger.vendor.dto.VendorResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface VendorService {

    VendorResponseDTO createVendor(VendorRequestDTO requestDTO);

    List<VendorResponseDTO> getAllVendors();

    ResponseEntity<VendorResponseDTO> getVendorById(Long id);

    ResponseEntity<VendorResponseDTO> updateVendor(Long id, VendorRequestDTO requestDTO);

    ResponseEntity<String> deleteVendor(Long id);

    List<VendorResponseDTO> getPendingVendors();

    ResponseEntity<?> acceptVendor(Long id);

    ResponseEntity<?> rejectVendor(Long id);

    void autoUpdateVendorStatus(Long vendorId);
}