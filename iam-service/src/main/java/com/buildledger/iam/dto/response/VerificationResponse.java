package com.buildledger.iam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Document / GST / PAN verification response")
public class VerificationResponse {

    @Schema(description = "Whether verification succeeded")
    private boolean verified;

    @Schema(description = "Verification method used", example = "API | MANUAL_PENDING")
    private String method;

    @Schema(description = "Details from verification API")
    private String details;

    @Schema(description = "Error message if verification failed")
    private String error;

    public static VerificationResponse verified(String method, String details) {
        return VerificationResponse.builder()
                .verified(true)
                .method(method)
                .details(details)
                .build();
    }

    public static VerificationResponse failed(String method, String error) {
        return VerificationResponse.builder()
                .verified(false)
                .method(method)
                .error(error)
                .build();
    }

    public static VerificationResponse pendingManual(String reason) {
        return VerificationResponse.builder()
                .verified(false)
                .method("MANUAL_PENDING")
                .details("External API unavailable. Flagged for manual admin review.")
                .error(reason)
                .build();
    }
}
