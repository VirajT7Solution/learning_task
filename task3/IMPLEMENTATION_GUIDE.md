# WebAuthn Passwordless Authentication - Complete Implementation Guide

## Overview

This guide provides a complete implementation of passwordless authentication using WebAuthn (FIDO2) passkeys in a Spring Boot application. The implementation includes all necessary components for a production-ready WebAuthn system.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Database Schema](#database-schema)
3. [Core Components](#core-components)
4. [API Endpoints](#api-endpoints)
5. [Security Implementation](#security-implementation)
6. [Client Integration](#client-integration)
7. [Testing Guide](#testing-guide)
8. [Production Deployment](#production-deployment)

## Architecture Overview

### System Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │    │   PostgreSQL    │
│   (WebAuthn)    │◄──►│   Application   │◄──►│   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   WebAuthn4J    │
                       │   Library       │
                       └─────────────────┘
```

### Key Features

- **Passwordless Authentication**: No passwords required
- **FIDO2/WebAuthn Standard**: Industry-standard implementation
- **Spring Security Integration**: Custom authentication provider
- **PostgreSQL Storage**: Secure credential storage
- **RESTful API**: Complete registration and authentication flows

## Database Schema

### Tables

#### 1. Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. WebAuthn Credentials Table
```sql
CREATE TABLE webauthn_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_id BYTEA NOT NULL,
    public_key BYTEA NOT NULL,
    sign_count BIGINT NOT NULL DEFAULT 0,
    transports TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, credential_id)
);
```

#### 3. WebAuthn Challenges Table
```sql
CREATE TABLE webauthn_challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge BYTEA NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('registration', 'authentication')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);
```

## Core Components

### 1. Entity Layer

#### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WebAuthnCredential> credentials = new ArrayList<>();
}
```

#### WebAuthnCredential Entity
```java
@Entity
@Table(name = "webauthn_credentials")
public class WebAuthnCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "credential_id", nullable = false, columnDefinition = "BYTEA")
    private byte[] credentialId;
    
    @Column(name = "public_key", nullable = false, columnDefinition = "BYTEA")
    private byte[] publicKey;
    
    @Column(name = "sign_count", nullable = false)
    private Long signCount = 0L;
}
```

### 2. Repository Layer

#### UserRepository
```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

#### WebAuthnCredentialRepository
```java
@Repository
public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, UUID> {
    Optional<WebAuthnCredential> findByCredentialId(byte[] credentialId);
    List<WebAuthnCredential> findByUserId(UUID userId);
    long countByUserId(UUID userId);
}
```

### 3. Service Layer

#### WebAuthnService
The core service handles all WebAuthn operations:

- **Registration Flow**: Generate options and verify attestation
- **Authentication Flow**: Generate options and verify assertion
- **Credential Management**: Store and validate credentials
- **Challenge Management**: Handle time-limited challenges

### 4. Controller Layer

#### WebAuthnController
REST endpoints for WebAuthn operations:

- `POST /webauthn/register/options` - Generate registration options
- `POST /webauthn/register/verify` - Verify registration
- `POST /webauthn/authenticate/options` - Generate authentication options
- `POST /webauthn/authenticate/verify` - Verify authentication

#### ProtectedController
Protected endpoints for authenticated users:

- `GET /api/user` - Get current user information
- `GET /api/profile` - Get user profile

### 5. Security Layer

#### WebAuthnAuthenticationProvider
Custom authentication provider that:

- Validates WebAuthn authentication tokens
- Verifies passkey credentials
- Creates authenticated sessions

#### SecurityConfig
Spring Security configuration with:

- WebAuthn authentication provider
- CORS configuration
- Session management
- Exception handling

## API Endpoints

### Registration Flow

#### 1. Generate Registration Options
```http
POST /webauthn/register/options
Content-Type: application/json

{
    "username": "john_doe",
    "email": "john@example.com",
    "displayName": "John Doe"
}
```

**Response:**
```json
{
    "challenge": "base64url-encoded-challenge",
    "rp": {
        "id": "localhost",
        "name": "WebAuthn Demo Application"
    },
    "user": {
        "id": "base64url-encoded-user-id",
        "name": "john_doe",
        "displayName": "John Doe"
    },
    "pubKeyCredParams": [
        "alg=-7&type=public-key",
        "alg=-257&type=public-key"
    ],
    "timeout": 300000,
    "attestation": ["direct", "indirect", "none"]
}
```

#### 2. Verify Registration
```http
POST /webauthn/register/verify
Content-Type: application/json

{
    "challenge": "base64url-encoded-challenge",
    "attestationResponse": "base64url-encoded-attestation-response"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Registration successful"
}
```

### Authentication Flow

#### 1. Generate Authentication Options
```http
POST /webauthn/authenticate/options
Content-Type: application/json

{
    "username": "john_doe"
}
```

**Response:**
```json
{
    "challenge": "base64url-encoded-challenge",
    "timeout": 300000,
    "rpId": "localhost",
    "allowCredentials": [
        {
            "id": "base64url-encoded-credential-id",
            "type": "public-key",
            "transports": ["usb", "nfc", "ble", "internal"]
        }
    ],
    "userVerification": "preferred"
}
```

#### 2. Verify Authentication
```http
POST /webauthn/authenticate/verify
Content-Type: application/json

{
    "challenge": "base64url-encoded-challenge",
    "assertionResponse": "base64url-encoded-assertion-response"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Authentication successful",
    "data": {
        "id": "user-uuid",
        "username": "john_doe",
        "email": "john@example.com"
    }
}
```

## Security Implementation

### WebAuthn Security Features

1. **Replay Attack Prevention**
   - Sign count validation
   - Challenge expiration
   - One-time use challenges

2. **Credential Security**
   - Credentials bound to specific users
   - Public key cryptography
   - Secure storage in database

3. **Challenge-Response Security**
   - Time-limited challenges
   - Random challenge generation
   - Challenge validation

### Spring Security Integration

#### Custom Authentication Provider
```java
@Component
public class WebAuthnAuthenticationProvider implements AuthenticationProvider {
    
    @Override
    public Authentication authenticate(Authentication authentication) {
        // Validate WebAuthn token
        // Verify passkey credential
        // Return authenticated token
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return WebAuthnAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

#### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/webauthn/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

## Client Integration

### Frontend Requirements

1. **WebAuthn API Support**: Modern browsers with WebAuthn support
2. **HTTPS Required**: WebAuthn requires secure context
3. **Credential Management**: Ability to create and use passkeys

### JavaScript Implementation

#### Registration Flow
```javascript
async function registerUser(userInfo) {
    // 1. Get registration options
    const optionsResponse = await fetch('/webauthn/register/options', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userInfo)
    });
    const options = await optionsResponse.json();
    
    // 2. Create credential
    const credential = await navigator.credentials.create({
        publicKey: {
            challenge: Uint8Array.from(atob(options.challenge), c => c.charCodeAt(0)),
            rp: options.rp,
            user: {
                id: Uint8Array.from(atob(options.user.id), c => c.charCodeAt(0)),
                name: options.user.name,
                displayName: options.user.displayName
            },
            pubKeyCredParams: options.pubKeyCredParams,
            timeout: options.timeout,
            attestation: options.attestation
        }
    });
    
    // 3. Verify registration
    const verifyResponse = await fetch('/webauthn/register/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            challenge: options.challenge,
            attestationResponse: credential
        })
    });
    
    return await verifyResponse.json();
}
```

#### Authentication Flow
```javascript
async function authenticateUser(username) {
    // 1. Get authentication options
    const optionsResponse = await fetch('/webauthn/authenticate/options', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username })
    });
    const options = await optionsResponse.json();
    
    // 2. Get assertion
    const assertion = await navigator.credentials.get({
        publicKey: {
            challenge: Uint8Array.from(atob(options.challenge), c => c.charCodeAt(0)),
            timeout: options.timeout,
            rpId: options.rpId,
            allowCredentials: options.allowCredentials.map(cred => ({
                id: Uint8Array.from(atob(cred.id), c => c.charCodeAt(0)),
                type: cred.type,
                transports: cred.transports
            })),
            userVerification: options.userVerification
        }
    });
    
    // 3. Verify authentication
    const verifyResponse = await fetch('/webauthn/authenticate/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            challenge: options.challenge,
            assertionResponse: assertion
        })
    });
    
    return await verifyResponse.json();
}
```

## Testing Guide

### Manual Testing

#### 1. Registration Test
```bash
# 1. Generate registration options
curl -X POST http://localhost:8080/webauthn/register/options \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","displayName":"Test User"}'

# 2. Use browser WebAuthn API to create credential
# 3. Verify registration
curl -X POST http://localhost:8080/webauthn/register/verify \
  -H "Content-Type: application/json" \
  -d '{"challenge":"...","attestationResponse":"..."}'
```

#### 2. Authentication Test
```bash
# 1. Generate authentication options
curl -X POST http://localhost:8080/webauthn/authenticate/options \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser"}'

# 2. Use browser WebAuthn API to authenticate
# 3. Verify authentication
curl -X POST http://localhost:8080/webauthn/authenticate/verify \
  -H "Content-Type: application/json" \
  -d '{"challenge":"...","assertionResponse":"..."}'
```

### Automated Testing

#### Unit Tests
```java
@SpringBootTest
class WebAuthnServiceTest {
    
    @Autowired
    private WebAuthnService webAuthnService;
    
    @Test
    void testGenerateRegistrationOptions() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        
        RegistrationOptionsResponse response = webAuthnService.generateRegistrationOptions(request);
        
        assertThat(response).isNotNull();
        assertThat(response.getChallenge()).isNotEmpty();
        assertThat(response.getRp()).isNotNull();
        assertThat(response.getUser()).isNotNull();
    }
}
```

#### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebAuthnControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testRegistrationOptionsEndpoint() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        
        ResponseEntity<RegistrationOptionsResponse> response = restTemplate.postForEntity(
            "/webauthn/register/options", request, RegistrationOptionsResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
```

## Production Deployment

### Security Considerations

1. **HTTPS Required**: WebAuthn requires HTTPS in production
2. **Domain Configuration**: Set proper relying party ID
3. **Credential Cleanup**: Implement credential cleanup for deleted users
4. **Rate Limiting**: Add rate limiting for authentication attempts
5. **Monitoring**: Monitor authentication success/failure rates

### Performance Optimization

1. **Database Indexing**: Ensure proper indexes on credential lookups
2. **Challenge Cleanup**: Regular cleanup of expired challenges
3. **Connection Pooling**: Configure appropriate database connection pooling
4. **Caching**: Consider caching for frequently accessed data

### Deployment Checklist

- [ ] HTTPS configured
- [ ] Database credentials secured
- [ ] CORS properly configured
- [ ] Rate limiting implemented
- [ ] Monitoring configured
- [ ] Logging configured
- [ ] Backup strategy implemented
- [ ] Security headers configured

## Troubleshooting

### Common Issues

1. **CORS Errors**: Ensure CORS is properly configured for your domain
2. **HTTPS Required**: WebAuthn requires HTTPS in production
3. **Browser Support**: Ensure browser supports WebAuthn
4. **Credential Storage**: Check if credentials are properly stored

### Debug Configuration

```properties
# Enable debug logging
logging.level.com.task3=DEBUG
logging.level.com.webauthn4j=DEBUG

# WebAuthn configuration
webauthn.rp.id=yourdomain.com
webauthn.rp.name=Your Application Name
webauthn.challenge.timeout=300
```

### Health Checks

```bash
# Check application health
curl http://localhost:8080/webauthn/health

# Check protected endpoints
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/health
```

## Conclusion

This implementation provides a complete, production-ready WebAuthn passwordless authentication system. The architecture is modular, secure, and follows industry best practices. The system can be extended with additional features like multi-factor authentication, credential management, and advanced security policies.

For production deployment, ensure proper security configuration, monitoring, and testing procedures are in place.
