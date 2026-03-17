package com.buildledger.iam.repository;

import com.buildledger.iam.entity.Vendor;
import com.buildledger.iam.entity.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByGstNumber(String gstNumber);

    Page<Vendor> findByStatus(VendorStatus status, Pageable pageable);

    Optional<Vendor> findByUserId(Long userId);

    @Query("SELECT v FROM Vendor v WHERE " +
           "(:search IS NULL OR LOWER(v.companyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.gstNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Vendor> searchVendors(@Param("search") String search, Pageable pageable);
}
