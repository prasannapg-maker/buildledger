package com.buildledger.delivery_service.service;

import com.buildledger.delivery_service.dto.*;
import com.buildledger.delivery_service.model.*;
import com.buildledger.delivery_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceTrackingService {

    private final ServiceRepository serviceRepository;

    @Transactional
    public ServiceResponse createServiceTask(CreateServiceRequest request) {
        ServiceTracking serviceTracking = ServiceTracking.builder()
                .contractId(request.getContractId())
                .description(request.getDescription())
                .status(ServiceStatus.PENDING)
                .build();

        return mapToResponse(serviceRepository.save(serviceTracking));
    }

    public List<ServiceResponse> getAllServices(Long contractId, ServiceStatus status) {
        List<ServiceTracking> services;
        if (contractId != null) {
            services = serviceRepository.findByContractId(contractId);
        } else if (status != null) {
            services = serviceRepository.findByStatus(status);
        } else {
            services = serviceRepository.findAll();
        }

        return services.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ServiceResponse getServiceById(Long serviceId) {
        return mapToResponse(serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found")));
    }

    @Transactional
    public ServiceResponse updateService(Long serviceId, CreateServiceRequest request) {
        ServiceTracking serviceTracking = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        
        if (request.getDescription() != null) serviceTracking.setDescription(request.getDescription());
        
        return mapToResponse(serviceRepository.save(serviceTracking));
    }

    @Transactional
    public ServiceResponse startService(Long serviceId) {
        ServiceTracking serviceTracking = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        // only allow starting from PENDING status
        if (serviceTracking.getStatus() != ServiceStatus.PENDING) {
            throw new IllegalStateException("Cannot start a service that is not in PENDING status");
        }
        serviceTracking.setStatus(ServiceStatus.IN_PROGRESS);
        return mapToResponse(serviceRepository.save(serviceTracking));
    }

    @Transactional
    public ServiceResponse completeService(Long serviceId, LocalDate completionDate) {
        ServiceTracking serviceTracking = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        // only allow completion from IN_PROGRESS status
        if (serviceTracking.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete a service that is not in IN_PROGRESS status");
        }
        serviceTracking.setStatus(ServiceStatus.COMPLETED);
        serviceTracking.setCompletionDate(completionDate);
        
        return mapToResponse(serviceRepository.save(serviceTracking));
    }

    @Transactional
    public ServiceResponse verifyService(Long serviceId) {
        ServiceTracking serviceTracking = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        // only allow verification from COMPLETED status
        if (serviceTracking.getStatus() != ServiceStatus.COMPLETED) {
            throw new IllegalStateException("Cannot verify a service that is not in COMPLETED status");
        }
        serviceTracking.setStatus(ServiceStatus.VERIFIED);
        return mapToResponse(serviceRepository.save(serviceTracking));
    }

    @Transactional
    public void deleteService(Long serviceId) {
        ServiceTracking serviceTracking = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        serviceRepository.delete(serviceTracking);
    }

    private ServiceResponse mapToResponse(ServiceTracking serviceTracking) {
        return ServiceResponse.builder()
                .serviceId(serviceTracking.getServiceId())
                .contractId(serviceTracking.getContractId())
                .description(serviceTracking.getDescription())
                .completionDate(serviceTracking.getCompletionDate())
                .status(serviceTracking.getStatus())
                .remarks(serviceTracking.getRemarks())
                .createdAt(serviceTracking.getCreatedAt())
                .updatedAt(serviceTracking.getUpdatedAt())
                .build();
    }
}
