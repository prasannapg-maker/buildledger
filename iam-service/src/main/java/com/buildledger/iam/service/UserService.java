package com.buildledger.iam.service;

import com.buildledger.iam.dto.request.CreateUserRequest;
import com.buildledger.iam.dto.request.UpdateUserRequest;
import com.buildledger.iam.dto.response.UserResponse;
import com.buildledger.iam.entity.AccountStatus;
import com.buildledger.iam.entity.User;
import com.buildledger.iam.entity.UserRole;
import com.buildledger.iam.exception.DuplicateResourceException;
import com.buildledger.iam.exception.InvalidRequestException;
import com.buildledger.iam.exception.ResourceNotFoundException;
import com.buildledger.iam.repository.UserRepository;
import com.buildledger.iam.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final EmailService emailService;
    private final AuditService auditService;

    @Value("${password.temp.length:12}")
    private int tempPasswordLength;

    /**
     * Admin creates a new internal user.
     * Generates a temporary password and sends a welcome email.
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, String createdByEmail, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists");
        }

        if (request.getRole() == UserRole.VENDOR || request.getRole() == UserRole.CLIENT) {
            throw new InvalidRequestException(
                    "Vendors and Clients must self-register. Cannot create " + request.getRole() + " via admin.");
        }

        String tempPassword = passwordGenerator.generateTemporaryPassword(tempPasswordLength);
        System.out.println("TEMP PASSWORD FOR " + request.getEmail() + ": " + tempPassword); // For testing purposes
                                                                                             // only

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(tempPassword))
                .role(request.getRole())
                .status(AccountStatus.FIRST_LOGIN_REQUIRED)
                .forcePasswordChange(true)
                .createdBy(createdByEmail)
                .build();

        user = userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getName(), tempPassword);
        auditService.logSuccess(null, "USER_CREATED", "users/" + user.getId(), ipAddress);
        log.info("User created by {}: id={}, email={}, role={}",
                createdByEmail, user.getId(), user.getEmail(), user.getRole());

        return UserResponse.from(user);
    }

    /**
     * List all users with pagination (admin).
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return userRepository.searchUsers(search.trim(), pageable).map(UserResponse::from);
        }
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));
    }

    /**
     * Update user profile (admin).
     */
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request,
            String updatedByEmail, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            if (request.getRole() == UserRole.VENDOR || request.getRole() == UserRole.CLIENT) {
                throw new InvalidRequestException("Cannot assign VENDOR or CLIENT role to internal users");
            }
            user.setRole(request.getRole());
        }

        user = userRepository.save(user);
        auditService.logSuccess(null, "USER_UPDATED", "users/" + userId, ipAddress);
        return UserResponse.from(user);
    }

    /**
     * Disable a user account (admin).
     */
    @Transactional
    public UserResponse disableUser(Long userId, String adminEmail, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        if (AccountStatus.DISABLED.equals(user.getStatus())) {
            throw new InvalidRequestException("User is already disabled");
        }

        user.setStatus(AccountStatus.DISABLED);
        user = userRepository.save(user);

        auditService.logSuccess(null, "USER_DISABLED", "users/" + userId, ipAddress);
        log.info("User {} disabled by {}", userId, adminEmail);
        return UserResponse.from(user);
    }

    /**
     * Enable a user account (admin).
     */
    @Transactional
    public UserResponse enableUser(Long userId, String adminEmail, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        user.setStatus(AccountStatus.ACTIVE);
        user.setAccountLocked(false);
        user.setLockTime(null);
        user.setFailedLoginAttempts(0);
        user = userRepository.save(user);

        auditService.logSuccess(null, "USER_ENABLED", "users/" + userId, ipAddress);
        log.info("User {} enabled by {}", userId, adminEmail);
        return UserResponse.from(user);
    }

    /**
     * Unlock a locked user account (admin).
     */
    @Transactional
    public UserResponse unlockUser(Long userId, String adminEmail, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        if (!Boolean.TRUE.equals(user.getAccountLocked()) &&
                !AccountStatus.LOCKED.equals(user.getStatus())) {
            throw new InvalidRequestException("User account is not locked");
        }

        user.resetFailedAttempts();
        user = userRepository.save(user);

        auditService.logSuccess(null, "USER_UNLOCKED", "users/" + userId, ipAddress);
        log.info("User {} unlocked by {}", userId, adminEmail);
        return UserResponse.from(user);
    }

    /**
     * Get user by ID for internal service use (minimal data).
     */
    @Transactional(readOnly = true)
    public UserResponse getInternalUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));
    }
}
