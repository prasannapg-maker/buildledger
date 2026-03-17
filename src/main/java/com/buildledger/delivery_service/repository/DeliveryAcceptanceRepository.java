package com.buildledger.delivery_service.repository;

import com.buildledger.delivery_service.model.DeliveryAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryAcceptanceRepository extends JpaRepository<DeliveryAcceptance, Long> {
}
