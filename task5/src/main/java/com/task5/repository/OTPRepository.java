package com.task5.repository;

import com.task5.config.OTPVerification;
import com.task5.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTPVerification, Long> {
    Optional<OTPVerification> findByUserAndOtpCodeAndIsUsedFalse(User user, String otpCode);
}