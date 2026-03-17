package com.buildledger.iam.service;

import com.buildledger.iam.entity.AccountStatus;
import com.buildledger.iam.entity.User;
import com.buildledger.iam.entity.UserRole;
import com.buildledger.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.email:admin@buildledger.com}")
    private String adminEmail;

    @Value("${admin.bootstrap.password:Admin@2024!}")
    private String adminPassword;

    @Value("${admin.bootstrap.name:System Administrator}")
    private String adminName;

    /**
     * Create default admin user on application startup if not already present.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapAdmin() {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ADMIN)
                    .status(AccountStatus.ACTIVE)
                    .forcePasswordChange(false)
                    .createdBy("SYSTEM")
                    .build();

            userRepository.save(admin);
            log.warn("=======================================================");
            log.warn("  Default ADMIN account created: {}", adminEmail);
            log.warn("  IMPORTANT: Change the admin password immediately!");
            log.warn("=======================================================");
        } else {
            log.info("Admin account already exists: {}", adminEmail);
        }
    }
}
