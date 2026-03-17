package com.buildledger.delivery_service.controller;

import com.buildledger.delivery_service.dto.*;
import com.buildledger.delivery_service.model.ServiceStatus;
import com.buildledger.delivery_service.service.ServiceTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Service Tracking", description = "Endpoints for managing and tracking service tasks")
public class ServiceController {

    private final ServiceTrackingService serviceTrackingService;

    @PostMapping
    @Operation(summary = "Create a new service task", description = "Creates a new service tracking record for a contract.")
    public ServiceResponse createService(@RequestBody CreateServiceRequest request) {
        return serviceTrackingService.createServiceTask(request);
    }

    @GetMapping
    @Operation(summary = "Get all service tasks", description = "Retrieves a list of all service tasks, optionally filtered by contract ID or status.")
    public List<ServiceResponse> getAllServices(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) ServiceStatus status) {
        return serviceTrackingService.getAllServices(contractId, status);
    }

    @GetMapping("/{serviceId}")
    @Operation(summary = "Get service task by ID", description = "Retrieves the details of a specific service task by its ID.")
    public ServiceResponse getServiceById(@PathVariable Long serviceId) {
        return serviceTrackingService.getServiceById(serviceId);
    }

    @PutMapping("/{serviceId}")
    @Operation(summary = "Update service task details", description = "Updates an existing service task record.")
    public ServiceResponse updateService(@PathVariable Long serviceId, @RequestBody CreateServiceRequest request) {
        return serviceTrackingService.updateService(serviceId, request);
    }

    @PatchMapping("/{serviceId}/start")
    @Operation(summary = "Start a service task", description = "Updates the status of a service task to IN_PROGRESS.")
    public ServiceResponse startService(@PathVariable Long serviceId) {
        return serviceTrackingService.startService(serviceId);
    }

    @PatchMapping("/{serviceId}/complete")
    @Operation(summary = "Complete a service task", description = "Marks a service task as COMPLETED with a specified completion date.")
    public ServiceResponse completeService(@PathVariable Long serviceId, @RequestBody CompleteServiceRequest request) {
        return serviceTrackingService.completeService(serviceId, request.getCompletionDate());
    }

    @PatchMapping("/{serviceId}/verify")
    @Operation(summary = "Verify a service task", description = "Marks a service task as VERIFIED after inspection.")
    public ServiceResponse verifyService(@PathVariable Long serviceId) {
        return serviceTrackingService.verifyService(serviceId);
    }

    @DeleteMapping("/{serviceId}")
    @Operation(summary = "Delete a service task", description = "Deletes a specific service task record.")
    public void deleteService(@PathVariable Long serviceId) {
        serviceTrackingService.deleteService(serviceId);
    }
}
