package com.buildledger.iam.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendors", indexes = {
        @Index(name = "idx_vendor_email", columnList = "email", unique = true),
        @Index(name = "idx_vendor_gst", columnList = "gstNumber"),
        @Index(name = "idx_vendor_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String companyName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String category;

    @Column(length = 20)
    private String gstNumber;

    @Column(length = 15)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VendorStatus status = VendorStatus.REGISTERED;

    @Column
    private Long userId;

    @Column
    private String rejectionReason;

    @Column
    private LocalDateTime approvedAt;

    @Column(length = 100)
    private String approvedBy;

    @Column
    @Builder.Default
    private Boolean gstVerified = false;

    @Column
    @Builder.Default
    private Boolean panVerified = false;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VendorDocument> documents = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
