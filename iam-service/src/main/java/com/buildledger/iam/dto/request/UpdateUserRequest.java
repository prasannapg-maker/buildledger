package com.buildledger.iam.dto.request;

import com.buildledger.iam.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request payload to update a user")
public class UpdateUserRequest {

    @Schema(description = "Full name", example = "John Doe Updated")
    private String name;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    @Schema(description = "Phone number", example = "+919876543210")
    private String phone;

    @Schema(description = "User role")
    private UserRole role;
}
