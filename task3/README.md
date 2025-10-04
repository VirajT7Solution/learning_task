# WebAuthn Passwordless Authentication Implementation

This Spring Boot application implements passwordless authentication using WebAuthn (FIDO2) passkeys.

## Features

- **Passwordless Authentication**: Users can authenticate using passkeys instead of passwords
- **WebAuthn/FIDO2 Support**: Full support for WebAuthn standard with FIDO2 authenticators
- **Spring Security Integration**: Custom authentication provider for WebAuthn
- **PostgreSQL Storage**: Secure storage of passkey credentials
- **RESTful API**: Complete REST API for registration and authentication flows

## Architecture

### Core Components

1. **Entities**: User, WebAuthnCredential, WebAuthnChallenge
2. **Repositories**: Data access layer for all entities
3. **Services**: WebAuthnService for business logic
4. **Controllers**: REST endpoints for WebAuthn operations
5. **Security**: Custom AuthenticationProvider and Security configuration

### Database Schema

The application uses PostgreSQL with the following tables:

- `users`: User information
- `webauthn_credentials`: Stored passkey credentials
- `webauthn_challenges`: Temporary challenges for registration/authentication

## API Endpoints

### Registration Flow

1. **POST /webauthn/register/options**
   - Generate registration options for a user
   - Returns challenge and relying party information

2. **POST /webauthn/register/verify**
   - Verify and store the passkey credential
   - Completes the registration process

### Authentication Flow

1. **POST /webauthn/authenticate/options**
   - Generate authentication options for a user
   - Returns challenge and allowed credentials

2. **POST /webauthn/authenticate/verify**
   - Verify the passkey authentication
   - Returns authentication result

### Protected Endpoints

- **GET /api/user**: Get current user information
- **GET /api/profile**: Get user profile
- **GET /api/health**: Health check for protected endpoints

## Configuration

### Application Properties

```properties
# WebAuthn Configuration
webauthn.rp.id=localhost
webauthn.rp.name=WebAuthn Demo Application
webauthn.challenge.timeout=300

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/task3
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Dependencies

- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- PostgreSQL
- WebAuthn4J (WebAuthn library)
- Lombok

## Setup Instructions

### 1. Database Setup

```sql
-- Run the schema.sql file to create the required tables
psql -U postgres -d task3 -f schema.sql
```

### 2. Application Configuration

1. Update `application.properties` with your database credentials
2. Configure the relying party ID and name for your domain
3. Set appropriate challenge timeout values

### 3. Running the Application

```bash
mvn spring-boot:run
```

## Security Considerations

### WebAuthn Security Features

- **Replay Attack Prevention**: Sign count validation
- **Challenge-Response**: Time-limited challenges
- **Credential Binding**: Credentials bound to specific users
- **Attestation**: Optional credential attestation

### Implementation Security

- **Input Validation**: All inputs are validated
- **Exception Handling**: Comprehensive error handling
- **Logging**: Security events are logged
- **CORS Configuration**: Proper CORS setup for web clients

## Client Integration

### Frontend Requirements

To use this WebAuthn implementation, you need a frontend that supports:

1. **WebAuthn API**: Modern browsers with WebAuthn support
2. **Credential Management**: Ability to create and use passkeys
3. **Challenge Handling**: Proper challenge generation and verification

### Example Frontend Flow

```javascript
// Registration
const options = await fetch('/webauthn/register/options', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, displayName })
}).then(r => r.json());

const credential = await navigator.credentials.create({
    publicKey: options
});

await fetch('/webauthn/register/verify', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        challenge: options.challenge,
        attestationResponse: credential
    })
});

// Authentication
const authOptions = await fetch('/webauthn/authenticate/options', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username })
}).then(r => r.json());

const assertion = await navigator.credentials.get({
    publicKey: authOptions
});

await fetch('/webauthn/authenticate/verify', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        challenge: authOptions.challenge,
        assertionResponse: assertion
    })
});
```

## Testing

### Manual Testing

1. **Registration Test**:
   - POST to `/webauthn/register/options` with user info
   - Use browser WebAuthn API to create credential
   - POST to `/webauthn/register/verify` with attestation

2. **Authentication Test**:
   - POST to `/webauthn/authenticate/options` with username
   - Use browser WebAuthn API to authenticate
   - POST to `/webauthn/authenticate/verify` with assertion

### Automated Testing

The application includes comprehensive exception handling and validation that can be tested with various scenarios:

- Invalid challenges
- Expired challenges
- Invalid credentials
- Malformed requests

## Production Considerations

### Security Hardening

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

## Troubleshooting

### Common Issues

1. **CORS Errors**: Ensure CORS is properly configured for your domain
2. **HTTPS Required**: WebAuthn requires HTTPS in production
3. **Browser Support**: Ensure browser supports WebAuthn
4. **Credential Storage**: Check if credentials are properly stored

### Debugging

Enable debug logging:

```properties
logging.level.com.task3=DEBUG
logging.level.com.webauthn4j=DEBUG
```

## License

This implementation is provided as an example for educational purposes.
