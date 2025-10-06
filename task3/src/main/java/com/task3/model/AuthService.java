package com.task3.model;

import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTotpEnabled(false);

        userRepository.save(user);

        return new RegisterResponse("User registered successfully", user.getUsername());
    }

    @Transactional
    public TotpSetupResponse setupTotp(String username) throws QrGenerationException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String secret = totpService.generateSecret();
        String qrCodeUri = totpService.generateQrCodeDataUri(secret, username);

        user.setTotpSecret(secret);
        userRepository.save(user);

        return new TotpSetupResponse(secret, qrCodeUri,
                "Scan this QR code with Google Authenticator or Microsoft Authenticator");
    }

    @Transactional
    public VerifyTotpResponse verifyAndEnableTotp(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTotpSecret() == null) {
            throw new RuntimeException("TOTP not set up. Please set up TOTP first");
        }

        boolean isValid = totpService.verifyCode(user.getTotpSecret(), code);

        if (isValid) {
            user.setTotpEnabled(true);
            userRepository.save(user);
            return new VerifyTotpResponse(true, "TOTP enabled successfully");
        } else {
            return new VerifyTotpResponse(false, "Invalid TOTP code");
        }
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // If TOTP is not enabled, login directly
        if (!user.isTotpEnabled()) {
            String token = jwtService.generateToken(user.getUsername());
            return new LoginResponse(token, "Login successful", false);
        }

        // If TOTP is enabled, verify the code
        if (request.getTotpCode() == null || request.getTotpCode().isEmpty()) {
            return new LoginResponse(null, "TOTP code required", true);
        }

        boolean isValidCode = totpService.verifyCode(user.getTotpSecret(), request.getTotpCode());

        if (!isValidCode) {
            throw new RuntimeException("Invalid TOTP code");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token, "Login successful", true);
    }

    @Transactional
    public String disableTotp(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);

        return "TOTP disabled successfully";
    }
}