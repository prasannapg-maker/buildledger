package com.buildledger.iam.service;

import com.buildledger.iam.dto.response.VerificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalVerificationService {

    private final RestTemplate restTemplate;

    @Value("${external.gst.api-url}")
    private String gstApiUrl;

    @Value("${external.gst.api-key}")
    private String gstApiKey;

    @Value("${external.pan.api-url}")
    private String panApiUrl;

    @Value("${external.pan.api-key}")
    private String panApiKey;

    @Value("${external.ocr.api-url}")
    private String ocrApiUrl;

    @Value("${external.ocr.api-key}")
    private String ocrApiKey;

    /**
     * Verify GST number via external API.
     * Falls back to manual review if API is unavailable.
     */
    public VerificationResponse verifyGst(String gstNumber) {
        log.info("Verifying GST: {}", gstNumber);
        try {
            String url = gstApiUrl + "/search/gstin/" + gstNumber;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + gstApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String status = (String) body.getOrDefault("sts", "");
                String legalName = (String) body.getOrDefault("lgnm", gstNumber);

                if ("Active".equalsIgnoreCase(status)) {
                    return VerificationResponse.verified("API",
                            "GST is Active. Legal Name: " + legalName);
                } else {
                    return VerificationResponse.failed("API",
                            "GST status is: " + status);
                }
            }
            return VerificationResponse.failed("API", "Empty response from GST API");

        } catch (RestClientException e) {
            log.warn("GST API unavailable for {}: {}. Flagging for manual review.", gstNumber, e.getMessage());
            return VerificationResponse.pendingManual("GST API unavailable: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during GST verification: {}", e.getMessage());
            return VerificationResponse.pendingManual("Unexpected error during GST verification");
        }
    }

    /**
     * Verify PAN via external API.
     * Falls back to manual review if API is unavailable.
     */
    public VerificationResponse verifyPan(String panNumber, String nameOnPan) {
        log.info("Verifying PAN: {}", panNumber);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", panApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("pan", panNumber);
            requestBody.put("name", nameOnPan);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    panApiUrl, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Boolean valid = (Boolean) body.getOrDefault("valid", false);
                String message = (String) body.getOrDefault("message", "");

                if (Boolean.TRUE.equals(valid)) {
                    return VerificationResponse.verified("API", "PAN verified: " + message);
                } else {
                    return VerificationResponse.failed("API", "PAN invalid: " + message);
                }
            }
            return VerificationResponse.failed("API", "Empty response from PAN API");

        } catch (RestClientException e) {
            log.warn("PAN API unavailable for {}: {}. Flagging for manual review.", panNumber, e.getMessage());
            return VerificationResponse.pendingManual("PAN API unavailable: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during PAN verification: {}", e.getMessage());
            return VerificationResponse.pendingManual("Unexpected error during PAN verification");
        }
    }

    /**
     * Perform OCR verification on uploaded document.
     * Sends file to external OCR API.
     * Falls back to manual review on failure.
     */
    public VerificationResponse performOcrVerification(MultipartFile file, String documentType) {
        log.info("Performing OCR verification for documentType={}", documentType);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", ocrApiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("language", "eng");
            body.add("isOverlayRequired", false);
            body.add("detectOrientation", true);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    ocrApiUrl, HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean isErroredOnProcessing = (Boolean) responseBody.getOrDefault("IsErroredOnProcessing", true);

                if (!Boolean.TRUE.equals(isErroredOnProcessing)) {
                    return VerificationResponse.verified("OCR_API",
                            "Document successfully scanned. Type: " + documentType);
                } else {
                    String errorMessage = (String) responseBody.getOrDefault("ErrorMessage", "OCR failed");
                    return VerificationResponse.failed("OCR_API", errorMessage);
                }
            }
            return VerificationResponse.failed("OCR_API", "Empty response from OCR API");

        } catch (RestClientException e) {
            log.warn("OCR API unavailable: {}. Flagging for manual review.", e.getMessage());
            return VerificationResponse.pendingManual("OCR API unavailable: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during OCR verification: {}", e.getMessage());
            return VerificationResponse.pendingManual("Unexpected error during OCR verification");
        }
    }
}
