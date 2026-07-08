package com.nv.task1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("EMS Ledger - Password Reset OTP");
        message.setText(
                "Your OTP to reset your EMS Ledger password is: " + otp +
                "\n\nThis OTP is valid for 10 minutes. If you didn't request this, you can safely ignore this email."
        );
        mailSender.send(message);
    }
}
