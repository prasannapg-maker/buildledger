package com.buildledger.delivery_service.service;

import com.buildledger.delivery_service.dto.*;
import com.buildledger.delivery_service.model.*;
import com.buildledger.delivery_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryAcceptanceRepository acceptanceRepository;

    @Transactional
    public DeliveryResponse createDelivery(CreateDeliveryRequest request) {
        Delivery delivery = Delivery.builder()
                .contractId(request.getContractId())
                .item(request.getItem())
                .quantity(request.getQuantity())
                .deliveryDate(request.getDeliveryDate())
                .expectedDate(request.getExpectedDate())
                .status(DeliveryStatus.PENDING)
                .build();

        delivery = deliveryRepository.save(delivery);

        return DeliveryResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .status(delivery.getStatus())
                .build();
    }

    public List<DeliveryDetailResponse> getAllDeliveries(Long contractId, DeliveryStatus status) {
        checkForDelays();
        List<Delivery> deliveries;
        if (contractId != null && status != null) {
            deliveries = deliveryRepository.findByContractIdAndStatus(contractId, status);
        } else if (contractId != null) {
            deliveries = deliveryRepository.findByContractId(contractId);
        } else if (status != null) {
            deliveries = deliveryRepository.findByStatus(status);
        } else {
            deliveries = deliveryRepository.findAll();
        }

        return deliveries.stream().map(this::mapToDetailResponse).collect(Collectors.toList());
    }

    public DeliveryDetailResponse getDeliveryById(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        return mapToDetailResponse(delivery);
    }

    @Transactional
    public DeliveryDetailResponse updateDelivery(Long deliveryId, CreateDeliveryRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (request.getQuantity() != null) delivery.setQuantity(request.getQuantity());
        if (request.getDeliveryDate() != null) delivery.setDeliveryDate(request.getDeliveryDate());
        if(request.getItem() != null) delivery.setItem(request.getItem());

        return mapToDetailResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryDetailResponse markAsDelivered(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        delivery.setStatus(DeliveryStatus.DELIVERED);
        return mapToDetailResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryDetailResponse acceptDelivery(Long deliveryId, String remarks) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        // only allow acceptance after the item has actually been delivered
        if (delivery.getStatus() != DeliveryStatus.DELIVERED) {
            // in a real application you might use a custom exception type
            throw new IllegalStateException("Cannot accept a delivery that has not been marked as delivered");
        }

        delivery.setStatus(DeliveryStatus.ACCEPTED);
        delivery.setRemarks(remarks);
        deliveryRepository.save(delivery);

        DeliveryAcceptance acceptance = DeliveryAcceptance.builder()
                .deliveryId(deliveryId)
                .status(AcceptanceStatus.ACCEPTED)
                .remarks(remarks)
                // In a real app, approvedBy would come from SecurityContext
                .approvedBy(1L) 
                .build();
        acceptanceRepository.save(acceptance);

        return mapToDetailResponse(delivery);
    }

    @Transactional
    public DeliveryDetailResponse rejectDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        // require that the item was actually delivered before a rejection is recorded
        if (delivery.getStatus() != DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Cannot reject a delivery that has not been marked as delivered");
        }
        delivery.setStatus(DeliveryStatus.REJECTED);
        
        DeliveryAcceptance acceptance = DeliveryAcceptance.builder()
                .deliveryId(deliveryId)
                .status(AcceptanceStatus.REJECTED)
                .approvedBy(1L)
                .build();
        acceptanceRepository.save(acceptance);

        return mapToDetailResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public void deleteDelivery(Long deliveryId) {
        deliveryRepository.deleteById(deliveryId);
    }

    @Transactional
    public void checkForDelays() {
        List<Delivery> pendingDeliveries = deliveryRepository.findByStatus(DeliveryStatus.PENDING);
        LocalDate today = LocalDate.now();
        for (Delivery delivery : pendingDeliveries) {
            if (delivery.getExpectedDate() != null && delivery.getExpectedDate().isBefore(today)) {
                delivery.setStatus(DeliveryStatus.DELAYED);
                deliveryRepository.save(delivery);
            }
        }
    }

    public Map<String, Long> getStatusSummary() {
        List<Delivery> deliveries = deliveryRepository.findAll();
        return deliveries.stream()
                .collect(Collectors.groupingBy(d -> d.getStatus().name().toLowerCase(), Collectors.counting()));
    }

    private DeliveryDetailResponse mapToDetailResponse(Delivery delivery) {
        return DeliveryDetailResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .contractId(delivery.getContractId())
                .item(delivery.getItem())
                .quantity(delivery.getQuantity())
                .deliveryDate(delivery.getDeliveryDate())
                .expectedDate(delivery.getExpectedDate())
                .status(delivery.getStatus())
                .remarks(delivery.getRemarks())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
