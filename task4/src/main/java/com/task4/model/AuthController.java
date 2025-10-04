package com.task4.model;

import com.task4.req.AuthenticationFinishRequest;
import com.task4.req.AuthenticationStartRequest;
import com.task4.req.RegistrationFinishRequest;
import com.task4.req.RegistrationStartRequest;
import com.yubico.webauthn.AssertionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yubico.webauthn.data.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final WebAuthnService webAuthnService;

    @PostMapping("/register/start")
    public ResponseEntity<?> startRegistration(@RequestBody RegistrationStartRequest request) {
        try {
            PublicKeyCredentialCreationOptions options =
                    webAuthnService.startRegistration(request.getUsername(), request.getEmail());
            return ResponseEntity.ok(options.toJson());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/finish")
    public ResponseEntity<?> finishRegistration(@RequestBody RegistrationFinishRequest request) {
        try {
            User user = webAuthnService.finishRegistration(
                    request.getUsername(),
                    request.getEmail(),
                    request.getCredential(),
                    request.getDeviceName()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", user.getId(),
                    "username", user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login/start")
    public ResponseEntity<?> startAuthentication(@RequestBody AuthenticationStartRequest request) {
        try {
            AssertionRequest assertionRequest = webAuthnService.startAuthentication(request.getUsername());
            return ResponseEntity.ok(assertionRequest.toJson());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login/finish")
    public ResponseEntity<?> finishAuthentication(@RequestBody AuthenticationFinishRequest request) {
        try {
            User user = webAuthnService.finishAuthentication(
                    request.getUsername(),
                    request.getCredential()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", user.getId(),
                    "username", user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}