package com.buildledger.vendor.repository;

import com.buildledger.vendor.enums.VendorStatus;
import com.buildledger.vendor.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    List<Vendor> findByStatus(VendorStatus status);
}
