package com.buildledger.iam.dto.response;

import com.buildledger.iam.entity.DocumentStatus;
import com.buildledger.iam.entity.VendorDocument;
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
@Schema(description = "Vendor document response")
public class VendorDocumentResponse {

    private Long id;
    private Long vendorId;
    private String documentType;
    private String originalFileName;
    private String fileUrl;
    private String fileExtension;
    private Long fileSizeBytes;
    private DocumentStatus status;
    private String rejectionReason;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private LocalDateTime uploadedAt;

    public static VendorDocumentResponse from(VendorDocument doc) {
        return VendorDocumentResponse.builder()
                .id(doc.getId())
                .vendorId(doc.getVendor().getId())
                .documentType(doc.getDocumentType())
                .originalFileName(doc.getOriginalFileName())
                .fileUrl(doc.getFileUrl())
                .fileExtension(doc.getFileExtension())
                .fileSizeBytes(doc.getFileSizeBytes())
                .status(doc.getStatus())
                .rejectionReason(doc.getRejectionReason())
                .verifiedAt(doc.getVerifiedAt())
                .verifiedBy(doc.getVerifiedBy())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
