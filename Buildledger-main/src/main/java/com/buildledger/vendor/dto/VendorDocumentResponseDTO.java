package com.buildledger.vendor.dto;

import com.buildledger.vendor.enums.DocumentType;
import com.buildledger.vendor.enums.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VendorDocumentResponseDTO {

    private Long documentId;
    private DocumentType docType;
    private String fileUri;
    private LocalDate uploadedDate;
    private VerificationStatus verificationStatus;
    private Long vendorId;
}
