
package com.nv.task1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends transactional emails via the Resend HTTP API (https://resend.com).
 *
 * We switched away from plain SMTP (JavaMailSender / smtp.gmail.com:587) because
 * Render's free web-service tier blocks all outbound SMTP ports (25, 465, 587)
 * as of Sept 26, 2025. Resend's API works over plain HTTPS (port 443), which is
 * never blocked, so OTP emails work fine on the free tier.
 *
 * Required environment variables:
 *   RESEND_API_KEY   - your Resend API key (starts with "re_")
 *   RESEND_FROM_EMAIL - the verified sender address/domain, e.g. "EMS Ledger <onboarding@resend.dev>"
 */
@Service
public class EmailService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "EMS Ledger - Password Reset OTP";
        String textBody = "Your OTP to reset your EMS Ledger password is: " + otp
                + "\n\nThis OTP is valid for 10 minutes. If you didn't request this, you can safely ignore this email.";

        String jsonBody = """
                {
                  "from": "%s",
                  "to": ["%s"],
                  "subject": "%s",
                  "text": "%s"
                }
                """.formatted(
                escapeJson(fromEmail),
                escapeJson(toEmail),
                escapeJson(subject),
                escapeJson(textBody)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Failed to send OTP email. Resend API responded with "
                        + response.statusCode() + ": " + response.body());
            }
        } catch (java.io.IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}