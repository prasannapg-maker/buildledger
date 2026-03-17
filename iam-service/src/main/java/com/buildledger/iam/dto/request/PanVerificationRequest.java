package com.buildledger.iam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "PAN verification request")
public class PanVerificationRequest {

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format")
    @Schema(description = "PAN number", example = "ABCDE1234F")
    private String panNumber;

    @NotBlank(message = "Name is required")
    @Schema(description = "Name as per PAN card")
    private String nameOnPan;
}
