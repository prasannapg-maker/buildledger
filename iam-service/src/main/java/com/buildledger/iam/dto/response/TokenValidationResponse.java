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
@Schema(description = "Token validation result for internal microservices")
public class TokenValidationResponse {

    @Schema(description = "Whether the token is valid", example = "true")
    private boolean valid;

    @Schema(description = "User ID extracted from token", example = "101")
    private Long userId;

    @Schema(description = "User role", example = "PROJECT_MANAGER")
    private UserRole role;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "Reason if invalid")
    private String reason;
}
