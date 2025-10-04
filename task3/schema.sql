-- PostgreSQL Schema for WebAuthn Passkey Authentication
-- This schema supports FIDO2/WebAuthn credential storage

-- Users table (basic user management)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- WebAuthn credentials table
CREATE TABLE IF NOT EXISTS webauthn_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_id BYTEA NOT NULL, -- Binary credential ID
    public_key BYTEA NOT NULL, -- Binary public key
    sign_count BIGINT NOT NULL DEFAULT 0,
    transports TEXT[], -- Array of transport types (usb, nfc, ble, internal)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure credential_id is unique per user
    UNIQUE(user_id, credential_id)
);

-- WebAuthn challenges table (for session management)
CREATE TABLE IF NOT EXISTS webauthn_challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge BYTEA NOT NULL, -- Binary challenge
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('registration', 'authentication')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    -- Index for cleanup
    INDEX idx_challenges_expires_at (expires_at)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_webauthn_credentials_user_id ON webauthn_credentials(user_id);
CREATE INDEX IF NOT EXISTS idx_webauthn_credentials_credential_id ON webauthn_credentials(credential_id);
CREATE INDEX IF NOT EXISTS idx_webauthn_challenges_user_id ON webauthn_challenges(user_id);
CREATE INDEX IF NOT EXISTS idx_webauthn_challenges_type ON webauthn_challenges(type);

-- Sample data (optional - for testing)
INSERT INTO users (username, email, display_name) VALUES 
('testuser', 'test@example.com', 'Test User')
ON CONFLICT (username) DO NOTHING;
