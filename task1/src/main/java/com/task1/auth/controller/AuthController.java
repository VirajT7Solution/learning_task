package com.task1.auth.controller;

import com.task1.auth.model.*;
import com.task1.auth.repository.UserRepository;
import com.task1.auth.request.AuthRequest;
import com.task1.auth.request.AuthResponse;
import com.task1.auth.model.RefreshToken;
import com.task1.auth.request.RefreshRequest;
import com.task1.auth.request.RegisterRequest;
import com.task1.auth.service.JwtService;
import com.task1.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "username_taken"));
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "email_taken"));
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of(req.getRole().toUpperCase()))
                .enabled(true)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        } catch (DisabledException e) {
            return ResponseEntity.status(403).body(Map.of("error", "user_disabled"));
        }

        User user = userRepository.findByUsername(req.getUsername()).orElseThrow();
        var extraClaims = Map.<String, Object>of("roles", user.getRoles());
        String accessToken = jwtService.generateAccessToken(user.getUsername(), extraClaims);
        String refreshJwt = jwtService.generateRefreshToken(user.getUsername());

        // persist refresh token record (long random token would be better; here we store signed JWT too)
        // You might prefer to store a random opaque token rather than a signed JWT for refresh tokens.
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshJwt)
                .user(user)
                .expiryDate(Instant.ofEpochMilli(jwtService.extractExpiration(refreshJwt).getTime()).plusMillis(0))
                .revoked(false)
                .build();
        refreshTokenService.createRefreshToken(user, refreshToken.getExpiryDate()); // we create another opaque token here
        // For simplicity, return the JWT refresh token we issued above. In production use opaque DB-backed tokens or rotating tokens.

        user.setPassword(null);
        AuthResponse resp = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshJwt)
                .expiresInSeconds(jwtService.extractExpiration(accessToken).getTime() / 1000 - Instant.now().getEpochSecond())
                .user(user)
                .build();

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        String refreshTokenStr = req.getRefreshToken();
        if (refreshTokenStr == null || !jwtService.isTokenValid(refreshTokenStr)) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_refresh_token"));
        }

        // Check DB-record for refresh token and whether it's revoked/expired.
        RefreshToken dbToken = refreshTokenService.findByToken(refreshTokenStr);
        if (dbToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "refresh_token_not_found"));
        }
        if (dbToken.isRevoked() || dbToken.getExpiryDate().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(Map.of("error", "refresh_token_revoked_or_expired"));
        }

        String username = jwtService.extractUsername(refreshTokenStr);
        User user = userRepository.findByUsername(username).orElseThrow();

        var extraClaims = Map.<String, Object>of("roles", user.getRoles());
        String newAccessToken = jwtService.generateAccessToken(username, extraClaims);

        AuthResponse resp = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenStr) // could rotate refresh token here
                .expiresInSeconds(jwtService.extractExpiration(newAccessToken).getTime() / 1000 - Instant.now().getEpochSecond())
                .build();
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest req) {
        String refreshTokenStr = req.getRefreshToken();
        RefreshToken dbToken = refreshTokenService.findByToken(refreshTokenStr);
        if (dbToken != null) {
            refreshTokenService.revokeRefreshToken(dbToken);
        }
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }
}
