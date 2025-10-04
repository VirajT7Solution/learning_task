package com.task4.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticatorRepository extends JpaRepository<Authenticator, Long> {
    Optional<Authenticator> findByCredentialId(String credentialId);
}