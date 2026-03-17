package com.buildledger.delivery_service.dto;

import com.buildledger.delivery_service.model.ServiceStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ServiceResponse {
    private Long serviceId;
    private Long contractId;
    private String description;
    private LocalDate completionDate;
    private ServiceStatus status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
