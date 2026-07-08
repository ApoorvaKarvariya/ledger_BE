package com.nv.task1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_otp")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is the User's username, which in this app is the same as their email
    private String username;

    private String otp;

    private LocalDateTime expiryTime;

    private boolean verified;

    private boolean used;
}
