package com.nv.task1.service;

import com.nv.task1.entity.PasswordResetOtp;
import com.nv.task1.entity.User;
import com.nv.task1.repository.PasswordResetOtpRepository;
import com.nv.task1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int OTP_VALID_MINUTES = 10;

    // Change password while already logged in (Employee, Manager or Admin)
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Resolves either a login username OR an employee's registered email to the real User
    private User resolveUser(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmployee_EmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("No account found for that username/email"));
    }

    // Step 1 of "forgot password": generate a 6-digit OTP and email it
    // Accepts either the login username OR the employee's registered email
    @Transactional
    public void forgotPassword(String usernameOrEmail) {
        User user = resolveUser(usernameOrEmail);

        // Always key the OTP record off the real username, not whatever the user typed
        String realUsername = user.getUsername();

        String otp = generateOtp();

        otpRepository.deleteByUsername(realUsername);

        PasswordResetOtp record = new PasswordResetOtp();
        record.setUsername(realUsername);
        record.setOtp(otp);
        record.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
        record.setVerified(false);
        record.setUsed(false);
        otpRepository.save(record);

        String emailTo = (user.getEmployee() != null && user.getEmployee().getEmail() != null)
                ? user.getEmployee().getEmail()
                : realUsername;

        emailService.sendOtpEmail(emailTo, otp);
    }

    // Step 2: verify the OTP the user typed in
    // usernameOrEmail: whatever the user originally typed on the forgot-password screen
    public void verifyOtp(String usernameOrEmail, String otp) {
        String realUsername = resolveUser(usernameOrEmail).getUsername();
        PasswordResetOtp record = latestOtp(realUsername);

        if (record.isUsed()) {
            throw new RuntimeException("This OTP has already been used. Please request a new one.");
        }
        if (record.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This OTP has expired. Please request a new one.");
        }
        if (!record.getOtp().equals(otp)) {
            throw new RuntimeException("Incorrect OTP.");
        }

        record.setVerified(true);
        otpRepository.save(record);
    }

    // Step 3: set the new password (OTP must already be verified via step 2)
    public void resetPassword(String usernameOrEmail, String otp, String newPassword) {
        User user = resolveUser(usernameOrEmail);
        String realUsername = user.getUsername();
        PasswordResetOtp record = latestOtp(realUsername);

        if (record.isUsed()) {
            throw new RuntimeException("This OTP has already been used. Please request a new one.");
        }
        if (record.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This OTP has expired. Please request a new one.");
        }
        if (!record.getOtp().equals(otp) || !record.isVerified()) {
            throw new RuntimeException("Please verify your OTP first.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        record.setUsed(true);
        otpRepository.save(record);
    }

    private PasswordResetOtp latestOtp(String username) {
        return otpRepository.findTopByUsernameOrderByIdDesc(username)
                .orElseThrow(() -> new RuntimeException("No OTP request found. Please request a new one."));
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
