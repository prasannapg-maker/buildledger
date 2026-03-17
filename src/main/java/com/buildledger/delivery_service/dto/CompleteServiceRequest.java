package com.buildledger.delivery_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CompleteServiceRequest {
    private LocalDate completionDate;
}