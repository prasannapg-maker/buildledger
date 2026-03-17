package com.buildledger.delivery_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_acceptance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAcceptance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long acceptanceId;

    private Long deliveryId;
    private Long approvedBy;

    @Enumerated(EnumType.STRING)
    private AcceptanceStatus status;

    private String remarks;

    @CreationTimestamp
    @Column(name = "approved_at", updatable = false)
    private LocalDateTime approvedAt;
}
