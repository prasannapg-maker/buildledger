package com.buildledger.iam.dto.request;

import com.buildledger.iam.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request payload to create an internal user")
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address", example = "john.doe@buildledger.com")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    @Schema(description = "Phone number", example = "+919876543210")
    private String phone;

    @NotNull(message = "Role is required")
    @Schema(description = "User role", example = "PROJECT_MANAGER")
    private UserRole role;
}
