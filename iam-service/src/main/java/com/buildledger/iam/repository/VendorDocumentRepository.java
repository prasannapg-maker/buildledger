package com.buildledger.iam.repository;

import com.buildledger.iam.entity.DocumentStatus;
import com.buildledger.iam.entity.VendorDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorDocumentRepository extends JpaRepository<VendorDocument, Long> {

    List<VendorDocument> findByVendorId(Long vendorId);

    Page<VendorDocument> findByVendorId(Long vendorId, Pageable pageable);

    List<VendorDocument> findByVendorIdAndStatus(Long vendorId, DocumentStatus status);

    boolean existsByVendorIdAndDocumentType(Long vendorId, String documentType);

    long countByVendorIdAndStatus(Long vendorId, DocumentStatus status);
}
