package com.buildledger.delivery_service.controller;

import com.buildledger.delivery_service.dto.*;
import com.buildledger.delivery_service.model.DeliveryStatus;
import com.buildledger.delivery_service.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery Management", description = "Endpoints for creating, updating, and tracking deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    @Operation(summary = "Create a new delivery", description = "Creates a new delivery record with the provided details.")
    public DeliveryResponse createDelivery(@RequestBody CreateDeliveryRequest request) {
        return deliveryService.createDelivery(request);
    }

    @GetMapping
    @Operation(summary = "Get all deliveries", description = "Retrieves a list of all deliveries, optionally filtered by contract ID or status.")
    public List<DeliveryDetailResponse> getAllDeliveries(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) DeliveryStatus status) {
        return deliveryService.getAllDeliveries(contractId, status);
    }

    @GetMapping("/{deliveryId}")
    @Operation(summary = "Get delivery by ID", description = "Retrieves the details of a specific delivery record by its ID.")
    public DeliveryDetailResponse getDeliveryById(@PathVariable Long deliveryId) {
        return deliveryService.getDeliveryById(deliveryId);
    }

    @PutMapping("/{deliveryId}")
    @Operation(summary = "Update delivery details", description = "Updates an existing delivery record with new information.")
    public DeliveryDetailResponse updateDelivery(@PathVariable Long deliveryId, @RequestBody CreateDeliveryRequest request) {
        return deliveryService.updateDelivery(deliveryId, request);
    }

    @PatchMapping("/{deliveryId}/mark-delivered")
    @Operation(summary = "Mark delivery as delivered", description = "Updates the status of a delivery to DELIVERED.")
    public DeliveryDetailResponse markDelivered(@PathVariable Long deliveryId) {
        return deliveryService.markAsDelivered(deliveryId);
    }

    @PatchMapping("/{deliveryId}/accept")
    @Operation(summary = "Accept a delivery", description = "Marks a delivery as accepted and records optional remarks.")
    public DeliveryDetailResponse acceptDelivery(@PathVariable Long deliveryId, @RequestBody Map<String, String> body) {
        return deliveryService.acceptDelivery(deliveryId, body.get("remarks"));
    }

    @PatchMapping("/{deliveryId}/reject")
    @Operation(summary = "Reject a delivery", description = "Marks a delivery as rejected.")
    public DeliveryDetailResponse rejectDelivery(@PathVariable Long deliveryId) {
        return deliveryService.rejectDelivery(deliveryId);
    }

    @DeleteMapping("/{deliveryId}")
    @Operation(summary = "Delete a delivery", description = "Deletes a specific delivery record.")
    public void deleteDelivery(@PathVariable Long deliveryId) {
        deliveryService.deleteDelivery(deliveryId);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get delivery status summary", description = "Retrieves a summary of delivery counts grouped by status.")
    public Map<String, Long> getSummary() {
        return deliveryService.getStatusSummary();
    }
}
