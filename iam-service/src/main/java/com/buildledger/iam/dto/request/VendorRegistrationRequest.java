package com.buildledger.iam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Vendor self-registration request")
public class VendorRegistrationRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200, message = "Company name must be 2-200 characters")
    @Schema(description = "Legal company name", example = "BuildTech Solutions Pvt Ltd")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Business email", example = "contact@buildtech.com")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
    @Schema(description = "Contact phone", example = "+919876543210")
    private String phone;

    @NotBlank(message = "Category is required")
    @Schema(description = "Business category", example = "Civil Construction")
    private String category;

    @NotBlank(message = "GST number is required")
    @Pattern(
        regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
        message = "GST number must be in valid format (e.g., 29ABCDE1234F1Z5)"
    )
    @Schema(description = "GSTIN number", example = "29ABCDE1234F1Z5")
    private String gstNumber;

    @Pattern(
        regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
        message = "PAN must be in valid format (e.g., ABCDE1234F)"
    )
    @Schema(description = "PAN number", example = "ABCDE1234F")
    private String panNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64)
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Password must contain digit, lowercase, uppercase, and special character"
    )
    @Schema(description = "Account password")
    private String password;
}
