package com.buildledger.delivery_service.controller;

import com.buildledger.delivery_service.dto.*;
import com.buildledger.delivery_service.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts/{contractId}")
@RequiredArgsConstructor
@Tag(name = "Contract Tracking", description = "Endpoints for tracking deliveries and services associated with a contract")
public class ContractTrackingController {

    private final DeliveryService deliveryService;
    private final ServiceTrackingService serviceTrackingService;

    @GetMapping("/deliveries")
    @Operation(summary = "Get all deliveries for a contract", description = "Retrieves a list of all deliveries associated with the specified contract ID.")
    public List<DeliveryDetailResponse> getContractDeliveries(@PathVariable Long contractId) {
        return deliveryService.getAllDeliveries(contractId, null);
    }

    @GetMapping("/services")
    @Operation(summary = "Get all services for a contract", description = "Retrieves a list of all service tracking records associated with the specified contract ID.")
    public List<ServiceResponse> getContractServices(@PathVariable Long contractId) {
        return serviceTrackingService.getAllServices(contractId, null);
    }
}
