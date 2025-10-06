package com.task3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Register Request
@Data
@NoArgsConstructor
@AllArgsConstructor
class RegisterRequest {
    private String username;
    private String email;
    private String password;
}

// Register Response
@Data
@NoArgsConstructor
@AllArgsConstructor
class RegisterResponse {
    private String message;
    private String username;
}

// Login Request
@Data
@NoArgsConstructor
@AllArgsConstructor
class LoginRequest {
    private String username;
    private String password;
    private String totpCode; // Optional, required only if TOTP is enabled
}

// Login Response
@Data
@NoArgsConstructor
@AllArgsConstructor
class LoginResponse {
    private String token;
    private String message;
    private boolean totpRequired;
}

// TOTP Setup Response
@Data
@NoArgsConstructor
@AllArgsConstructor
class TotpSetupResponse {
    private String secret;
    private String qrCodeUri;
    private String message;
}

// Verify TOTP Request
@Data
@NoArgsConstructor
@AllArgsConstructor
class VerifyTotpRequest {
    private String code;
}

// Verify TOTP Response
@Data
@NoArgsConstructor
@AllArgsConstructor
class VerifyTotpResponse {
    private boolean verified;
    private String message;
}

// Disable TOTP Request
@Data
@NoArgsConstructor
@AllArgsConstructor
class DisableTotpRequest {
    private String password;
}