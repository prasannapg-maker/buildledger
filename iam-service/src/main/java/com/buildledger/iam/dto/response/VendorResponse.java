package com.buildledger.iam.dto.response;

import com.buildledger.iam.entity.Vendor;
import com.buildledger.iam.entity.VendorStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vendor profile response")
public class VendorResponse {

    private Long id;
    private String companyName;
    private String email;
    private String phone;
    private String category;
    private String gstNumber;
    private String panNumber;
    private VendorStatus status;
    private Boolean gstVerified;
    private Boolean panVerified;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VendorResponse from(Vendor vendor) {
        return VendorResponse.builder()
                .id(vendor.getId())
                .companyName(vendor.getCompanyName())
                .email(vendor.getEmail())
                .phone(vendor.getPhone())
                .category(vendor.getCategory())
                .gstNumber(vendor.getGstNumber())
                .panNumber(vendor.getPanNumber())
                .status(vendor.getStatus())
                .gstVerified(vendor.getGstVerified())
                .panVerified(vendor.getPanVerified())
                .approvedAt(vendor.getApprovedAt())
                .approvedBy(vendor.getApprovedBy())
                .rejectionReason(vendor.getRejectionReason())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }
}
