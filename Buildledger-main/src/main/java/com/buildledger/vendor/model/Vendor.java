package com.buildledger.vendor.model;

import com.buildledger.vendor.enums.VendorCategory;
import com.buildledger.vendor.enums.VendorStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vendors")
public class Vendor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus status;
}
