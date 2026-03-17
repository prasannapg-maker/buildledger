package com.buildledger.delivery_service.repository;

import com.buildledger.delivery_service.model.Delivery;
import com.buildledger.delivery_service.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByContractId(Long contractId);
    List<Delivery> findByStatus(DeliveryStatus status);
    List<Delivery> findByContractIdAndStatus(Long contractId, DeliveryStatus status);
}
