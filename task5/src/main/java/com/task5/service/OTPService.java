package com.task5.service;

import com.task5.config.OTPGenerator;
import com.task5.config.OTPVerification;
import com.task5.model.User;
import com.task5.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OTPService {

    private final OTPRepository otpRepository;
    private final JavaMailSender mailSender;

    private int otpExpirationMinutes=5;

    public void generateAndSendOTP(User user) {
        String otp = OTPGenerator.generateOTP();

        OTPVerification otpVerification = new OTPVerification();
        otpVerification.setUser(user);
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        otpRepository.save(otpVerification);

        sendEmail(user.getEmail(), otp);
    }

    private void sendEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("virajgajera@t7solution.com");
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + ". It is valid for " + otpExpirationMinutes + " minutes.");
        mailSender.send(message);
    }

    public void verifyOTP(User user, String otp) {
        OTPVerification otpRecord = otpRepository.findByUserAndOtpCodeAndIsUsedFalse(user, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        otpRecord.setUsed(true);
        otpRepository.save(otpRecord);

        user.setEnabled(true);
    }
}

