package com.buildledger.iam.controller;

import com.buildledger.iam.dto.request.CreateUserRequest;
import com.buildledger.iam.dto.request.UpdateUserRequest;
import com.buildledger.iam.dto.response.ApiResponse;
import com.buildledger.iam.dto.response.AuditLogResponse;
import com.buildledger.iam.dto.response.UserResponse;
import com.buildledger.iam.security.UserPrincipal;
import com.buildledger.iam.service.AuditService;
import com.buildledger.iam.service.UserService;
import com.buildledger.iam.util.IpAddressUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – User Management", description = "ADMIN only: Create, manage, and control user accounts")
@SecurityRequirement(name = "BearerAuth")
public class AdminUserController {

    private final UserService userService;
    private final AuditService auditService;

    // ────────────────────────────────────────────────────────
    //  User CRUD
    // ────────────────────────────────────────────────────────

    @PostMapping("/users")
    @Operation(summary = "Create a new internal user",
               description = "Admin creates an internal user (PROJECT_MANAGER, FINANCE_OFFICER, etc.). A temporary password is generated and emailed to the user. On first login, the user must set a new password.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        UserResponse user = userService.createUser(request, principal.getEmail(), ip);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users with pagination",
               description = "Supports optional search by name or email.")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @Parameter(description = "Search by name or email") @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Page<UserResponse> users = userService.getAllUsers(search, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user profile (name, phone, role)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        UserResponse updated = userService.updateUser(userId, request, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    // ────────────────────────────────────────────────────────
    //  Account status management
    // ────────────────────────────────────────────────────────

    @PatchMapping("/users/{userId}/disable")
    @Operation(summary = "Disable a user account")
    public ResponseEntity<ApiResponse<UserResponse>> disableUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        UserResponse user = userService.disableUser(userId, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("User disabled successfully", user));
    }

    @PatchMapping("/users/{userId}/enable")
    @Operation(summary = "Enable a user account")
    public ResponseEntity<ApiResponse<UserResponse>> enableUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        UserResponse user = userService.enableUser(userId, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("User enabled successfully", user));
    }

    @PatchMapping("/users/{userId}/unlock")
    @Operation(summary = "Unlock a locked user account",
               description = "Resets failed login attempt counter and clears account lock. Status set to ACTIVE.")
    public ResponseEntity<ApiResponse<UserResponse>> unlockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        UserResponse user = userService.unlockUser(userId, principal.getEmail(), ip);
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", user));
    }

    // ────────────────────────────────────────────────────────
    //  Audit Logs
    // ────────────────────────────────────────────────────────

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs with optional filters",
               description = "Filter by userId, action, date range. Sorted by timestamp descending.")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<AuditLogResponse> logs = auditService.getAuditLogs(
                userId, action, from, to, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
