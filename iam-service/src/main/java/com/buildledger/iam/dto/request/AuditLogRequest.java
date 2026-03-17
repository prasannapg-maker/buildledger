package com.buildledger.iam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Audit log entry from internal microservices")
public class AuditLogRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User who performed the action")
    private Long userId;

    @NotBlank(message = "Action is required")
    @Schema(description = "Action performed", example = "CONTRACT_CREATED")
    private String action;

    @Schema(description = "Resource affected", example = "contracts/42")
    private String resource;

    @Schema(description = "IP address of the request")
    private String ipAddress;

    @Schema(description = "Additional details or metadata")
    private String details;

    @Schema(description = "Outcome", example = "SUCCESS")
    private String outcome;
}
