package com.nv.task1.repository;

import com.nv.task1.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByUsernameOrderByIdDesc(String username);

    @Modifying
    void deleteByUsername(String username);
}
