package com.buildledger.delivery_service.repository;

import com.buildledger.delivery_service.model.ServiceTracking;
import com.buildledger.delivery_service.model.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceTracking, Long> {
    List<ServiceTracking> findByContractId(Long contractId);
    List<ServiceTracking> findByStatus(ServiceStatus status);
}
