package com.buildledger.iam.dto.response;

import com.buildledger.iam.entity.AuditLog;
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
@Schema(description = "Audit log entry response")
public class AuditLogResponse {

    private Long auditId;
    private Long userId;
    private String action;
    private String resource;
    private String ipAddress;
    private String details;
    private String outcome;
    private LocalDateTime timestamp;

    public static AuditLogResponse from(AuditLog log) {
        return AuditLogResponse.builder()
                .auditId(log.getAuditId())
                .userId(log.getUserId())
                .action(log.getAction())
                .resource(log.getResource())
                .ipAddress(log.getIpAddress())
                .details(log.getDetails())
                .outcome(log.getOutcome())
                .timestamp(log.getTimestamp())
                .build();
    }
}
