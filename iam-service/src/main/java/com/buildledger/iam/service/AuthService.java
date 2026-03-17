package com.buildledger.iam.service;

import com.buildledger.iam.dto.request.*;
import com.buildledger.iam.dto.response.LoginResponse;
import com.buildledger.iam.dto.response.TokenValidationResponse;
import com.buildledger.iam.entity.*;
import com.buildledger.iam.exception.*;
import com.buildledger.iam.repository.*;
import com.buildledger.iam.security.JwtTokenProvider;
import com.buildledger.iam.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final RateLimitingService rateLimitingService;
    private final AuditService auditService;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;

    @Value("${password.reset.token-expiry-minutes:15}")
    private int resetTokenExpiryMinutes;

    /**
     * Authenticate user and issue JWT tokens.
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        // Rate limit check
        rateLimitingService.checkLoginRateLimit(ipAddress);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditService.logFailure(null, "LOGIN", request.getEmail(), ipAddress, "User not found");
                    return new AuthenticationFailedException("Invalid email or password");
                });

        // Check account status before validating password
        checkAccountStatus(user);

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user, ipAddress);
            throw new AuthenticationFailedException("Invalid email or password");
        }

        // Successful authentication – reset failed attempts
        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = generateAndSaveRefreshToken(user);

        // Reset rate limiter
        rateLimitingService.reset(ipAddress);

        auditService.logSuccess(user.getId(), "LOGIN", "users/" + user.getId(), ipAddress);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .forcePasswordChange(Boolean.TRUE.equals(user.getForcePasswordChange()) ||
                        AccountStatus.FIRST_LOGIN_REQUIRED.equals(user.getStatus()))
                .user(LoginResponse.UserSummary.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .build();
    }

    /**
     * Logout: revoke the access token and all refresh tokens for the user.
     */
    @Transactional
    public void logout(String accessToken, Long userId, String ipAddress) {
        // Blacklist current access token
        try {
            String tokenId = jwtTokenProvider.extractTokenId(accessToken);
            Date expiry = jwtTokenProvider.extractExpiration(accessToken);
            tokenBlacklistService.revokeToken(tokenId, userId, expiry);
        } catch (Exception e) {
            log.warn("Could not extract token details during logout: {}", e.getMessage());
        }

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUserId(userId);

        auditService.logSuccess(userId, "LOGOUT", "users/" + userId, ipAddress);
        log.info("User {} logged out from {}", userId, ipAddress);
    }

    /**
     * Refresh access token using a valid refresh token.
     */
    @Transactional
    public LoginResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or already used"));

        if (!storedToken.isValid()) {
            throw new InvalidTokenException("Refresh token has expired or been revoked");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        checkAccountStatus(user);

        // Revoke old refresh token and issue new one
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = generateAndSaveRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .forcePasswordChange(Boolean.TRUE.equals(user.getForcePasswordChange()))
                .user(LoginResponse.UserSummary.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .build();
    }

    /**
     * Change password (for authenticated user, including forced first-login change).
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new PasswordMismatchException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirmation do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setForcePasswordChange(false);

        // If first login, activate account
        if (AccountStatus.FIRST_LOGIN_REQUIRED.equals(user.getStatus())) {
            user.setStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        // Revoke all refresh tokens (force re-login)
        refreshTokenRepository.revokeAllByUserId(userId);

        auditService.logSuccess(userId, "PASSWORD_CHANGED", "users/" + userId, ipAddress);
        log.info("Password changed for userId={}", userId);
    }

    /**
     * Initiate password reset: generate token and send email.
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request, String ipAddress) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Invalidate existing tokens
            passwordResetTokenRepository.invalidateExistingTokens(user.getId());

            String rawToken = passwordGenerator.generateSecureToken(64);
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userId(user.getId())
                    .token(rawToken)
                    .expiryDate(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), rawToken);
            auditService.logSuccess(user.getId(), "PASSWORD_RESET_REQUESTED", "users/" + user.getId(), ipAddress);
        });
        // Always return success to prevent user enumeration
    }

    /**
     * Complete password reset using token.
     */
    @Transactional
    public void resetPassword(PasswordResetConfirmRequest request, String ipAddress) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (!resetToken.isValid()) {
            throw new InvalidTokenException("Password reset token has expired or already been used");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> ResourceNotFoundException.user(resetToken.getUserId()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setForcePasswordChange(false);
        if (AccountStatus.FIRST_LOGIN_REQUIRED.equals(user.getStatus())) {
            user.setStatus(AccountStatus.ACTIVE);
        }
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllByUserId(user.getId());

        auditService.logSuccess(user.getId(), "PASSWORD_RESET_COMPLETED", "users/" + user.getId(), ipAddress);
    }

    /**
     * Validate a JWT token for internal microservice use.
     */
    public TokenValidationResponse validateToken(String token) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                return TokenValidationResponse.builder()
                        .valid(false).reason("Token is invalid or expired").build();
            }

            String tokenId = jwtTokenProvider.extractTokenId(token);
            if (tokenBlacklistService.isTokenRevoked(tokenId)) {
                return TokenValidationResponse.builder()
                        .valid(false).reason("Token has been revoked").build();
            }

            Long userId = jwtTokenProvider.extractUserId(token);
            String role = jwtTokenProvider.extractRole(token);
            String email = jwtTokenProvider.extractEmail(token);

            // Verify user still exists and is active
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || !user.isActive()) {
                return TokenValidationResponse.builder()
                        .valid(false).reason("User account is inactive or not found").build();
            }

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .role(user.getRole())
                    .email(email)
                    .build();

        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false).reason("Token validation error").build();
        }
    }

    // ───────────────────────── Private helpers ─────────────────────────

    private String generateAndSaveRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(rawToken)
                .expiryDate(expiryDate)
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private void checkAccountStatus(User user) {
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new AccountLockedException(
                    "Account locked due to multiple failed login attempts. Contact admin to unlock.");
        }
        if (AccountStatus.DISABLED.equals(user.getStatus())) {
            throw new AccountDisabledException("Account has been disabled. Contact your administrator.");
        }
        if (AccountStatus.INACTIVE.equals(user.getStatus())) {
            throw new AccountDisabledException("Account is inactive.");
        }
    }

    private void handleFailedLogin(User user, String ipAddress) {
        user.incrementFailedAttempts();

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.lockAccount();
            userRepository.save(user);

            emailService.sendAccountLockedEmail(user.getEmail(), user.getName());
            auditService.logFailure(user.getId(), "ACCOUNT_LOCKED",
                    "users/" + user.getId(), ipAddress,
                    "Locked after " + MAX_FAILED_ATTEMPTS + " failed attempts");

            throw new AccountLockedException(
                    "Account locked due to multiple failed login attempts. Contact admin to unlock.");
        }

        userRepository.save(user);
        auditService.logFailure(user.getId(), "LOGIN_FAILED",
                "users/" + user.getId(), ipAddress,
                "Invalid password – attempt " + user.getFailedLoginAttempts());
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredResetTokens() {
        int count = passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up {} expired password reset tokens", count);
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        int count = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up {} expired refresh tokens", count);
    }
}
