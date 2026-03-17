package com.buildledger.iam.dto.response;

import com.buildledger.iam.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response containing tokens and user info")
public class LoginResponse {

    @Schema(description = "JWT access token (valid 15 min)")
    private String accessToken;

    @Schema(description = "JWT refresh token (valid 7 days)")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiry in seconds", example = "900")
    private long expiresIn;

    @Schema(description = "Authenticated user info")
    private UserSummary user;

    @Schema(description = "Whether user must change password on first login")
    private boolean forcePasswordChange;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
        private UserRole role;
    }
}
