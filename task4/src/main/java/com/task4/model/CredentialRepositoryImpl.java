package com.task4.model;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.data.exception.HexException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CredentialRepositoryImpl implements CredentialRepository {

    private final UserRepository userRepository;
    private final AuthenticatorRepository authenticatorRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return userRepository.findByUsername(username).map(user -> user.getAuthenticators().stream().map(auth -> {
            try {
                return PublicKeyCredentialDescriptor.builder().id(ByteArray.fromBase64Url(auth.getCredentialId())).build();
            } catch (Base64UrlException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepository.findByUsername(username).map(user -> {
            try {
                return ByteArray.fromHex(user.getUserHandle());
            } catch (HexException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return userRepository.findByUserHandle(userHandle.getHex()).map(User::getUsername);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return authenticatorRepository.findByCredentialId(credentialId.getBase64Url()).filter(auth -> auth.getUser().getUserHandle().equals(userHandle.getHex())).map(auth -> RegisteredCredential.builder().credentialId(credentialId).userHandle(userHandle).publicKeyCose(ByteArray.fromBase64(auth.getPublicKey())).signatureCount(auth.getSignCount()).build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return authenticatorRepository.findByCredentialId(credentialId.getBase64Url()).map(auth -> {
            try {
                return RegisteredCredential.builder().credentialId(credentialId).userHandle(ByteArray.fromHex(auth.getUser().getUserHandle())).publicKeyCose(ByteArray.fromBase64(auth.getPublicKey())).signatureCount(auth.getSignCount()).build();
            } catch (HexException e) {
                throw new RuntimeException(e);
            }
        }).map(Set::of).orElse(Collections.emptySet());
    }
}