package com.buildledger.delivery_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateDeliveryRequest {
    private Long contractId;
    private String item;
    private Integer quantity;
    private LocalDate deliveryDate;
    private LocalDate expectedDate;
}
