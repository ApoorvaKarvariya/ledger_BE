package com.nv.task1.controller.api;

import com.nv.task1.EmployeeDTO.PasswordDTO;
import com.nv.task1.entity.User;
import com.nv.task1.repository.UserRepository;
import com.nv.task1.security.JwtUtil;
import com.nv.task1.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }

            String role = user.getRole().name();
            String token = jwtUtil.generateToken(username, role);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", role,
                    "username", username,
                    "employeeId", user.getEmployee() != null ? user.getEmployee().getId() : 0L,
                    "name", user.getEmployee() != null ? user.getEmployee().getName() : username
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    // Change password while logged in - works for Employee, Manager and Admin alike

    // Change password while logged in - works for Employee, Manager and Admin alike
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordDTO dto, Authentication auth) {
        try {
            passwordService.changePassword(auth.getName(), dto.getOldPassword(), dto.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Forgot password step 1: request an OTP by email. Body: { "username": "..." }
    // (username IS the email in this app, see EmployeeService.saveEmployee)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            passwordService.forgotPassword(body.get("username"));
            return ResponseEntity.ok(Map.of("message", "OTP sent to your registered email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Forgot password step 2: verify the OTP. Body: { "username": "...", "otp": "..." }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        try {
            passwordService.verifyOtp(body.get("username"), body.get("otp"));
            return ResponseEntity.ok(Map.of("message", "OTP verified"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Forgot password step 3: set the new password.
    // Body: { "username": "...", "otp": "...", "newPassword": "..." }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            passwordService.resetPassword(body.get("username"), body.get("otp"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}