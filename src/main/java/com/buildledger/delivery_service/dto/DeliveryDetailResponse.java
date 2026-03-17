package com.buildledger.delivery_service.dto;

import com.buildledger.delivery_service.model.DeliveryStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DeliveryDetailResponse {
    private Long deliveryId;
    private Long contractId;
    private String item;
    private Integer quantity;
    private LocalDate deliveryDate;
    private LocalDate expectedDate;
    private DeliveryStatus status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
