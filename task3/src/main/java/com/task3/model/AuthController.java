package com.task3.model;

import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new RegisterResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, e.getMessage(), false));
        }
    }

    @PostMapping("/totp/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(Authentication authentication) {
        try {
            String username = authentication.getName();
            TotpSetupResponse response = authService.setupTotp(username);
            return ResponseEntity.ok(response);
        } catch (QrGenerationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TotpSetupResponse(null, null, "Error generating QR code"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new TotpSetupResponse(null, null, e.getMessage()));
        }
    }

    @PostMapping("/totp/verify")
    public ResponseEntity<VerifyTotpResponse> verifyTotp(
            @RequestBody VerifyTotpRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            VerifyTotpResponse response = authService.verifyAndEnableTotp(username, request.getCode());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new VerifyTotpResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/totp/disable")
    public ResponseEntity<String> disableTotp(
            @RequestBody DisableTotpRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            String message = authService.disableTotp(username, request.getPassword());
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok("Hello, " + username + "! This is a protected endpoint.");
    }
}