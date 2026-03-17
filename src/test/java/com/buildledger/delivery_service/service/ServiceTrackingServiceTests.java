package com.buildledger.delivery_service.service;

import com.buildledger.delivery_service.model.ServiceTracking;
import com.buildledger.delivery_service.model.ServiceStatus;
import com.buildledger.delivery_service.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class ServiceTrackingServiceTests {

    @Mock
    private ServiceRepository serviceRepository;

    private ServiceTrackingService serviceTrackingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceTrackingService = new ServiceTrackingService(serviceRepository);
    }

    @Test
    void startServiceFromPendingShouldWork() {
        ServiceTracking service = ServiceTracking.builder()
                .serviceId(1L)
                .status(ServiceStatus.PENDING)
                .build();
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceTracking.class))).thenReturn(service);

        // This should work without throwing exception
        serviceTrackingService.startService(1L);
    }

    @Test
    void startServiceFromNonPendingShouldThrow() {
        ServiceTracking service = ServiceTracking.builder()
                .serviceId(1L)
                .status(ServiceStatus.COMPLETED)
                .build();
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceTracking.class))).thenReturn(service);

        assertThrows(IllegalStateException.class, () -> {
            serviceTrackingService.startService(1L);
        });
    }

    @Test
    void completeServiceFromInProgressShouldWork() {
        ServiceTracking service = ServiceTracking.builder()
                .serviceId(1L)
                .status(ServiceStatus.IN_PROGRESS)
                .build();
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceTracking.class))).thenReturn(service);

        // This should work without throwing exception
        serviceTrackingService.completeService(1L, LocalDate.now());
    }

    @Test
    void completeServiceFromNonInProgressShouldThrow() {
        ServiceTracking service = ServiceTracking.builder()
                .serviceId(1L)
                .status(ServiceStatus.PENDING)
                .build();
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceTracking.class))).thenReturn(service);

        assertThrows(IllegalStateException.class, () -> {
            serviceTrackingService.completeService(1L, LocalDate.now());
        });
    }

    @Test
    void verifyServiceFromCompletedShouldWork() {
        ServiceTracking service = ServiceTracking.builder()
                .serviceId(1L)
                .status(ServiceStatus.COMPLETED)
                .build();
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceTracking.class))).thenReturn(service);

        // This should work without throwing exception
        serviceTrackingService.verifyService(1L);
    }

    @Test
    void verifyServiceFromNonCompletedShouldThrow() {
        ServiceTracking service = ServiceTracking.builder()
                .serviceId(1L)
                .status(ServiceStatus.IN_PROGRESS)
                .build();
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceTracking.class))).thenReturn(service);

        assertThrows(IllegalStateException.class, () -> {
            serviceTrackingService.verifyService(1L);
        });
    }

    @Test
    void deleteServiceFromNonExistentIdShouldThrow() {
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            serviceTrackingService.deleteService(999L);
        });
    }
}