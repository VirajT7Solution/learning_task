package com.task4.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.data.exception.HexException;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WebAuthnService {

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final AuthenticatorRepository authenticatorRepository;

    // Store registration/authentication challenges temporarily
    private final Map<String, PublicKeyCredentialCreationOptions> registrationChallenges = new ConcurrentHashMap<>();
    private final Map<String, AssertionRequest> authenticationChallenges = new ConcurrentHashMap<>();

    @Transactional
    public PublicKeyCredentialCreationOptions startRegistration(String username, String email) throws HexException {
        // Check if user exists
        Optional<User> existingUser = userRepository.findByUsername(username);

        ByteArray userHandle;
        if (existingUser.isPresent()) {
            userHandle = ByteArray.fromHex(existingUser.get().getUserHandle());
        } else {
            // Generate random user handle
            byte[] handleBytes = new byte[32];
            new SecureRandom().nextBytes(handleBytes);
            userHandle = new ByteArray(handleBytes);
        }

        UserIdentity userIdentity = UserIdentity.builder()
                .name(username)
                .displayName(username)
                .id(userHandle)
                .build();

        StartRegistrationOptions registrationOptions = StartRegistrationOptions.builder()
                .user(userIdentity)
                .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                        .residentKey(ResidentKeyRequirement.REQUIRED)
                        .userVerification(UserVerificationRequirement.REQUIRED)
                        .build())
                .build();

        PublicKeyCredentialCreationOptions registration = relyingParty.startRegistration(registrationOptions);

        // Store challenge
        registrationChallenges.put(username, registration);

        return registration;
    }

    @Transactional
    public User finishRegistration(String username, String email, String credentialJson, String deviceName) {
        PublicKeyCredentialCreationOptions request = registrationChallenges.get(username);
        if (request == null) {
            throw new RuntimeException("No registration in progress for user: " + username);
        }

        try {
            // Log the raw credential JSON for debugging
            System.out.println("Raw credential JSON received: " + credentialJson);

            // Parse the credential response
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                    PublicKeyCredential.parseRegistrationResponseJson(credentialJson);

            System.out.println("Pkc JSON received: " + pkc.toString());

            FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build();

            System.out.println("option JSON received: " + options);
            RegistrationResult result = relyingParty.finishRegistration(options);

            System.out.println("result JSON received: " + result);

            // Create or update user
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        String hex = request.getUser().getId().getHex();

                        User newUser = new User();
                        newUser.setUsername(username);
                        newUser.setEmail(email);
                        newUser.setUserHandle(hex);
                        return newUser;
                    });

            System.out.println("user");
            // Save authenticator
            Authenticator authenticator = new Authenticator();
            authenticator.setCredentialId(result.getKeyId().getId().getBase64Url());
            authenticator.setPublicKey(result.getPublicKeyCose().getBase64());
            authenticator.setSignCount(result.getSignatureCount());
            authenticator.setAaguid(result.getAaguid().getHex());
            authenticator.setUser(user);
            authenticator.setDeviceName(deviceName);

            user.getAuthenticators().add(authenticator);
            user = userRepository.save(user);

            // Clean up challenge
            registrationChallenges.remove(username);

            return user;

        } catch (RegistrationFailedException e) {
            System.err.println("Registration failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration validation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            System.err.println("Failed to parse credential JSON: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Invalid credential format: " + e.getMessage(), e);
        }
    }

    public AssertionRequest startAuthentication(String username) {
        StartAssertionOptions options = StartAssertionOptions.builder()
                .username(username)
                .build();

        AssertionRequest request = relyingParty.startAssertion(options);
        authenticationChallenges.put(username, request);

        return request;
    }

    @Transactional
    public User finishAuthentication(String username, String credentialJson) {
        AssertionRequest request = authenticationChallenges.get(username);
        if (request == null) {
            throw new RuntimeException("No authentication in progress for user: " + username);
        }

        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                    PublicKeyCredential.parseAssertionResponseJson(credentialJson);

            FinishAssertionOptions options = FinishAssertionOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build();

            AssertionResult result = relyingParty.finishAssertion(options);

            if (result.isSuccess()) {
                // Update signature count
                Authenticator authenticator = authenticatorRepository
                        .findByCredentialId(result.getCredentialId().getBase64Url())
                        .orElseThrow(() -> new RuntimeException("Authenticator not found"));

                authenticator.setSignCount(result.getSignatureCount());
                authenticator.setLastUsedAt(Instant.now());
                authenticatorRepository.save(authenticator);

                // Clean up challenge
                authenticationChallenges.remove(username);

                return authenticator.getUser();
            } else {
                throw new RuntimeException("Authentication failed");
            }

        } catch (AssertionFailedException | IOException e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }
}