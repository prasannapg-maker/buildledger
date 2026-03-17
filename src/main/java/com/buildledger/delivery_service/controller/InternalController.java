package com.buildledger.delivery_service.controller;

import com.buildledger.delivery_service.dto.*;
import com.buildledger.delivery_service.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/contracts/{contractId}")
@RequiredArgsConstructor
@Tag(name = "Internal APIs", description = "Endpoints for internal service-to-service communication")
public class InternalController {

    private final DeliveryService deliveryService;
    private final ServiceTrackingService serviceTrackingService;

    @GetMapping("/deliveries/status")
    @Operation(summary = "Check delivery status (Internal)", description = "Inter-service endpoint to check delivery status for a contract.")
    public List<DeliveryDetailResponse> checkDeliveryStatus(@PathVariable Long contractId) {
        return deliveryService.getAllDeliveries(contractId, null);
    }

    @GetMapping("/services/status")
    @Operation(summary = "Check service status (Internal)", description = "Inter-service endpoint to check service status for a contract.")
    public List<ServiceResponse> checkServiceStatus(@PathVariable Long contractId) {
        return serviceTrackingService.getAllServices(contractId, null);
    }

    @GetMapping("/deliveries")
    @Operation(summary = "Get deliveries for invoice (Internal)", description = "Retrieves deliveries that are eligible for invoicing for a specific contract.")
    public List<DeliveryDetailResponse> getDeliveriesForInvoice(@PathVariable Long contractId) {
        return deliveryService.getAllDeliveries(contractId, null);
    }
}
