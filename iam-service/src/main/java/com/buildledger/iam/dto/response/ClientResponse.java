package com.buildledger.iam.dto.response;

import com.buildledger.iam.entity.Client;
import com.buildledger.iam.entity.ClientStatus;
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
@Schema(description = "Client registration response")
public class ClientResponse {

    private Long id;
    private String companyName;
    private String email;
    private String phone;
    private String projectDescription;
    private ClientStatus status;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClientResponse from(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .companyName(client.getCompanyName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .projectDescription(client.getProjectDescription())
                .status(client.getStatus())
                .rejectionReason(client.getRejectionReason())
                .approvedAt(client.getApprovedAt())
                .approvedBy(client.getApprovedBy())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
