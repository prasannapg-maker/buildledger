package com.buildledger.delivery_service.service;

import com.buildledger.delivery_service.model.Delivery;
import com.buildledger.delivery_service.model.DeliveryStatus;
import com.buildledger.delivery_service.repository.DeliveryAcceptanceRepository;
import com.buildledger.delivery_service.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class DeliveryServiceTests {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryAcceptanceRepository acceptanceRepository;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deliveryService = new DeliveryService(deliveryRepository, acceptanceRepository);
    }

    @Test
    void acceptBeforeDeliveredShouldThrow() {
        Delivery d = Delivery.builder()
                .deliveryId(1L)
                .status(DeliveryStatus.PENDING)
                .build();
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(d));

        assertThrows(IllegalStateException.class, () -> {
            deliveryService.acceptDelivery(1L, "ok");
        });
    }

    @Test
    void rejectBeforeDeliveredShouldThrow() {
        Delivery d = Delivery.builder()
                .deliveryId(2L)
                .status(DeliveryStatus.PENDING)
                .build();
        when(deliveryRepository.findById(2L)).thenReturn(Optional.of(d));

        assertThrows(IllegalStateException.class, () -> {
            deliveryService.rejectDelivery(2L);
        });
    }
}
