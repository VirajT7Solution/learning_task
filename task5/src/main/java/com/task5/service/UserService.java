package com.task5.service;

import com.task5.model.User;
import com.task5.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final OTPService otpService;

    public User registerUser(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password); // For production: hash the password!
        user.setEnabled(false);

        userRepository.save(user);

        otpService.generateAndSendOTP(user); // send OTP

        return user;
    }
}
