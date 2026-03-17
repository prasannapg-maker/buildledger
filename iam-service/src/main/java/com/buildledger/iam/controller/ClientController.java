package com.buildledger.iam.controller;

import com.buildledger.iam.dto.request.ClientRegistrationRequest;
import com.buildledger.iam.dto.response.ApiResponse;
import com.buildledger.iam.dto.response.ClientResponse;
import com.buildledger.iam.entity.ClientStatus;
import com.buildledger.iam.security.UserPrincipal;
import com.buildledger.iam.service.ClientService;
import com.buildledger.iam.util.IpAddressUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Client", description = "Client self-registration and admin management")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @Operation(summary = "Client self-registration (public)",
               description = "External client registers their company. Status set to PENDING_APPROVAL.")
    public ResponseEntity<ApiResponse<ClientResponse>> registerClient(
            @Valid @RequestBody ClientRegistrationRequest request,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        ClientResponse client = clientService.registerClient(request, ip);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client registration submitted. Pending approval.", client));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all clients (admin)",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Page<ClientResponse>>> getAllClients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ClientStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return ResponseEntity.ok(ApiResponse.success(
                clientService.getAllClients(search, status, PageRequest.of(page, size, sort))));
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get client by ID (admin)",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(clientService.getClientById(clientId)));
    }

    @PatchMapping("/{clientId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a client registration (admin)",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ClientResponse>> approveClient(
            @PathVariable Long clientId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        ClientResponse client = clientService.approveClient(clientId, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("Client approved successfully", client));
    }

    @PatchMapping("/{clientId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a client registration (admin)",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ClientResponse>> rejectClient(
            @PathVariable Long clientId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        ClientResponse client = clientService.rejectClient(clientId, reason, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("Client rejected", client));
    }
}
