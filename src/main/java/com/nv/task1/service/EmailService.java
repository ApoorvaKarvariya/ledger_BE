package com.nv.task1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends transactional emails via the Brevo (formerly Sendinblue) HTTP API
 * (https://www.brevo.com).
 *
 * We use Brevo instead of Resend because Brevo only requires verifying a
 * single sender EMAIL ADDRESS (e.g. your own Gmail), not a whole domain.
 * That means OTP emails can be sent to ANY recipient, not just your own
 * inbox - unlike Resend's no-domain "onboarding@resend.dev" test mode,
 * which only delivers to the account owner's email.
 *
 * We also don't use SMTP (JavaMailSender / smtp.gmail.com:587) because
 * Render's free web-service tier blocks all outbound SMTP ports
 * (25, 465, 587) as of Sept 26, 2025. Brevo's API works over plain HTTPS
 * (port 443), which is never blocked, so this works fine on the free tier.
 *
 * Required environment variables:
 *   BREVO_API_KEY    - your Brevo API key (starts with "xkeysib-")
 *   BREVO_FROM_EMAIL  - the single email address you verified in Brevo
 *                        (e.g. "apoorvalku05@gmail.com")
 *   BREVO_FROM_NAME   - display name for the sender, e.g. "EMS Ledger"
 */
@Service
public class EmailService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name}")
    private String fromName;

    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "EMS Ledger - Password Reset OTP";
        String textBody = "Your OTP to reset your EMS Ledger password is: " + otp
                + "\n\nThis OTP is valid for 10 minutes. If you didn't request this, you can safely ignore this email.";

        String jsonBody = """
                {
                  "sender": { "name": "%s", "email": "%s" },
                  "to": [ { "email": "%s" } ],
                  "subject": "%s",
                  "textContent": "%s"
                }
                """.formatted(
                escapeJson(fromName),
                escapeJson(fromEmail),
                escapeJson(toEmail),
                escapeJson(subject),
                escapeJson(textBody)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("api-key", brevoApiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Failed to send OTP email. Brevo API responded with "
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