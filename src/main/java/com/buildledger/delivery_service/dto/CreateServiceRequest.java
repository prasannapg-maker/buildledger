package com.buildledger.delivery_service.dto;

import lombok.Data;

@Data
public class CreateServiceRequest {
    private Long contractId;
    private String description;
}
