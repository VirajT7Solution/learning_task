package com.task4.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "authenticators")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class Authenticator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String credentialId; // Base64URL encoded

    @Lob
    @Column(nullable = false)
    private String publicKey; // COSE encoded public key

    @Column(nullable = false)
    private Long signCount;

    @Column(nullable = false)
    private String aaguid; // Authenticator Attestation GUID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String deviceName;
    private Instant createdAt;
    private Instant lastUsedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        lastUsedAt = Instant.now();
    }
}