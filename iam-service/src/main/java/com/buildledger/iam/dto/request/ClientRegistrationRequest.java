package com.buildledger.iam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Client self-registration request")
public class ClientRegistrationRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200, message = "Company name must be 2-200 characters")
    @Schema(description = "Client company name", example = "XYZ Infrastructure Corp")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Business email", example = "contact@xyz-infra.com")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
    @Schema(description = "Contact phone", example = "+919876543210")
    private String phone;

    @Size(max = 2000, message = "Project description must not exceed 2000 characters")
    @Schema(description = "Description of project or requirements")
    private String projectDescription;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64)
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Password must contain digit, lowercase, uppercase, and special character"
    )
    @Schema(description = "Account password")
    private String password;
}
