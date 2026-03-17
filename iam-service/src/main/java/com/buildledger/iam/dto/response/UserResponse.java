package com.buildledger.iam.dto.response;

import com.buildledger.iam.entity.AccountStatus;
import com.buildledger.iam.entity.User;
import com.buildledger.iam.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response")
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private AccountStatus status;
    private Boolean accountLocked;
    private Integer failedLoginAttempts;
    private LocalDateTime lockTime;
    private Boolean forcePasswordChange;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .accountLocked(user.getAccountLocked())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockTime(user.getLockTime())
                .forcePasswordChange(user.getForcePasswordChange())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .build();
    }
}
