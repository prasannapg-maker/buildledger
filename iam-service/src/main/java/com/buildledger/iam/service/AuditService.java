package com.buildledger.iam.service;

import com.buildledger.iam.dto.request.AuditLogRequest;
import com.buildledger.iam.dto.response.AuditLogResponse;
import com.buildledger.iam.entity.AuditLog;
import com.buildledger.iam.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an action asynchronously to avoid blocking the main request.
     */
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String resource, String ipAddress,
                    String details, String outcome) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .resource(resource)
                    .ipAddress(ipAddress)
                    .details(details)
                    .outcome(outcome)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to persist audit log: action={}, userId={}, error={}",
                    action, userId, e.getMessage());
        }
    }

    /**
     * Log from internal microservice request.
     */
    @Transactional
    public AuditLogResponse logFromRequest(AuditLogRequest request) {
        AuditLog entry = AuditLog.builder()
                .userId(request.getUserId())
                .action(request.getAction())
                .resource(request.getResource())
                .ipAddress(request.getIpAddress())
                .details(request.getDetails())
                .outcome(request.getOutcome())
                .build();
        return AuditLogResponse.from(auditLogRepository.save(entry));
    }

    /**
     * Retrieve paginated audit logs with optional filters.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Long userId, String action,
                                                LocalDateTime from, LocalDateTime to,
                                                Pageable pageable) {
        return auditLogRepository.searchAuditLogs(userId, action, from, to, pageable)
                .map(AuditLogResponse::from);
    }

    // Convenience shortcuts
    public void logSuccess(Long userId, String action, String resource, String ip) {
        log(userId, action, resource, ip, null, "SUCCESS");
    }

    public void logFailure(Long userId, String action, String resource, String ip, String reason) {
        log(userId, action, resource, ip, reason, "FAILURE");
    }
}
