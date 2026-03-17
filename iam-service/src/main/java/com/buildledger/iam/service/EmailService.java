package com.buildledger.iam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@buildledger.com}")
    private String fromEmail;

    @Async("taskExecutor")
    public void sendWelcomeEmail(String to, String name, String temporaryPassword) {
        String subject = "Welcome to BuildLedger – Your Account Credentials";
        String body = """
                Dear %s,
                
                Welcome to BuildLedger – Construction Contract & Vendor Management System.
                
                Your account has been created. Please use the following temporary credentials to log in:
                
                Email:    %s
                Password: %s
                
                IMPORTANT: You will be required to change your password on first login.
                
                Login at: https://app.buildledger.com/login
                
                If you did not expect this email, please contact support immediately.
                
                Regards,
                BuildLedger Team
                """.formatted(name, to, temporaryPassword);
        send(to, subject, body);
    }

    @Async("taskExecutor")
    public void sendPasswordResetEmail(String to, String name, String resetToken) {
        String subject = "BuildLedger – Password Reset Request";
        String body = """
                Dear %s,
                
                We received a request to reset your BuildLedger account password.
                
                Use the following reset token in the password reset form:
                
                Token: %s
                
                This token is valid for 15 minutes.
                
                If you did not request this, you can safely ignore this email.
                
                Regards,
                BuildLedger Team
                """.formatted(name, resetToken);
        send(to, subject, body);
    }

    @Async("taskExecutor")
    public void sendAccountLockedEmail(String to, String name) {
        String subject = "BuildLedger – Account Locked";
        String body = """
                Dear %s,
                
                Your BuildLedger account has been locked due to multiple failed login attempts.
                
                Please contact your administrator to unlock your account.
                
                Regards,
                BuildLedger Team
                """.formatted(name);
        send(to, subject, body);
    }

    @Async("taskExecutor")
    public void sendVendorApprovalEmail(String to, String companyName, String status, String reason) {
        String subject = "BuildLedger – Vendor Registration Update";
        String body = """
                Dear %s,
                
                Your vendor registration with BuildLedger has been %s.
                
                %s
                
                For queries, please contact vendor.support@buildledger.com.
                
                Regards,
                BuildLedger Team
                """.formatted(
                        companyName,
                        status.toLowerCase(),
                        reason != null ? "Reason: " + reason : "You can now access vendor services."
                );
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Non-blocking: log and continue
        }
    }
}
