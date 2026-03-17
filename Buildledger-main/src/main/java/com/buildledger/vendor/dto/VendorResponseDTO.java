package com.buildledger.vendor.dto;

import com.buildledger.vendor.enums.VendorCategory;
import com.buildledger.vendor.enums.VendorStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonPropertyOrder({"vendorId", "name", "contactInfo", "category", "status", "createdAt", "updatedAt"})
public class VendorResponseDTO {

    private Long vendorId;
    private String name;
    private String contactInfo;
    private VendorCategory category;
    private VendorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
