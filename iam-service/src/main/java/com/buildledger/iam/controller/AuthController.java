package com.buildledger.iam.controller;

import com.buildledger.iam.dto.request.*;
import com.buildledger.iam.dto.response.ApiResponse;
import com.buildledger.iam.dto.response.LoginResponse;
import com.buildledger.iam.security.UserPrincipal;
import com.buildledger.iam.service.AuthService;
import com.buildledger.iam.util.IpAddressUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, token refresh, and password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT tokens",
               description = "Validates credentials, checks account status, enforces rate limiting, and returns access + refresh tokens.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        LoginResponse response = authService.login(request, ip);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke tokens",
               description = "Adds the current access token to the blacklist and revokes all refresh tokens for the user.",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);
        String ip = IpAddressUtil.getClientIp(httpRequest);
        authService.logout(token, principal.getUserId(), ip);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PutMapping("/password")
    @Operation(summary = "Change password for authenticated user",
               description = "Used for both regular password changes and forced first-login password changes.",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        authService.changePassword(principal.getUserId(), request, ip);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully. Please login again.", null));
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "Request a password reset email",
               description = "Sends a password reset token to the registered email. Always returns success to prevent email enumeration.")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        authService.requestPasswordReset(request, ip);
        return ResponseEntity.ok(ApiResponse.success(
                "If the email is registered, a password reset link has been sent.", null));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password using token from email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request,
            HttpServletRequest httpRequest) {

        String ip = IpAddressUtil.getClientIp(httpRequest);
        authService.resetPassword(request, ip);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please login with your new password.", null));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
