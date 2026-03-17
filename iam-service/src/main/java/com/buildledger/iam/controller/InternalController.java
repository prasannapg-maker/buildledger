package com.buildledger.iam.controller;

import com.buildledger.iam.dto.request.AuditLogRequest;
import com.buildledger.iam.dto.response.ApiResponse;
import com.buildledger.iam.dto.response.AuditLogResponse;
import com.buildledger.iam.dto.response.ClientResponse;
import com.buildledger.iam.dto.response.TokenValidationResponse;
import com.buildledger.iam.dto.response.UserResponse;
import com.buildledger.iam.dto.response.VendorResponse;
import com.buildledger.iam.service.AuditService;
import com.buildledger.iam.service.AuthService;
import com.buildledger.iam.service.ClientService;
import com.buildledger.iam.service.UserService;
import com.buildledger.iam.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal API endpoints for consumption by other microservices within the BuildLedger ecosystem.
 * These endpoints require a valid Bearer JWT token (the calling service passes the user's token).
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Tag(name = "Internal – Microservice APIs",
     description = "Endpoints for internal consumption by Vendor, Contract, Finance, Compliance, and other services")
@SecurityRequirement(name = "BearerAuth")
public class InternalController {

    private final AuthService authService;
    private final UserService userService;
    private final VendorService vendorService;
    private final ClientService clientService;
    private final AuditService auditService;

    // ────────────────────────────────────────────────────────
    //  Token Validation
    // ────────────────────────────────────────────────────────

    @GetMapping("/auth/validate-token")
    @Operation(summary = "Validate JWT token and return user context",
               description = "Called by downstream microservices to validate a user's token. Returns userId, role, and email on success.")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        TokenValidationResponse result = authService.validateToken(token);

        // Return 401 if invalid so other services can handle appropriately
        if (!result.isValid()) {
            return ResponseEntity.status(401).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // ────────────────────────────────────────────────────────
    //  User lookup
    // ────────────────────────────────────────────────────────

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user profile by ID",
               description = "Used by downstream services to resolve user info from a userId in a JWT claim.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getInternalUser(userId)));
    }

    // ────────────────────────────────────────────────────────
    //  Vendor lookup
    // ────────────────────────────────────────────────────────

    @GetMapping("/vendors/{vendorId}")
    @Operation(summary = "Get vendor profile by ID")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorById(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getVendorById(vendorId)));
    }

    @GetMapping("/vendors/{vendorId}/status")
    @Operation(summary = "Get vendor status only (lightweight)",
               description = "Returns just the vendor status. Used by Contract Service to check vendor approval before contract creation.")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorStatus(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getVendorStatus(vendorId)));
    }

    // ────────────────────────────────────────────────────────
    //  Audit logging
    // ────────────────────────────────────────────────────────

    @PostMapping("/audit")
    @Operation(summary = "Submit an audit log entry from another microservice",
               description = "Contract Service, Finance Service etc. call this to persist audit events in the central audit log.")
    public ResponseEntity<ApiResponse<AuditLogResponse>> submitAuditLog(
            @Valid @RequestBody AuditLogRequest request) {

        AuditLogResponse log = auditService.logFromRequest(request);
        return ResponseEntity.ok(ApiResponse.success("Audit log recorded", log));
    }
}
