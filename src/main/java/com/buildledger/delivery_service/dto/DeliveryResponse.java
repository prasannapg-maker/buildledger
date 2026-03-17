package com.buildledger.delivery_service.dto;

import com.buildledger.delivery_service.model.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryResponse {
    private Long deliveryId;
    private DeliveryStatus status;
}
