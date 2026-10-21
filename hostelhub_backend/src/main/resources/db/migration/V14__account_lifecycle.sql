
-- V14__account_lifecycle.sql
-- Rename V8 to the next unused Flyway version.

CREATE TABLE account_activation_tokens (
                                           id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           token_hash VARCHAR(128) NOT NULL UNIQUE,
                                           expires_at TIMESTAMPTZ NOT NULL,
                                           used_at TIMESTAMPTZ,
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activation_user_id
    ON account_activation_tokens(user_id);

CREATE INDEX idx_activation_expiry
    ON account_activation_tokens(expires_at);


CREATE TABLE password_reset_otps (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                     otp_hash VARCHAR(128) NOT NULL,
                                     expires_at TIMESTAMPTZ NOT NULL,
                                     attempts INTEGER NOT NULL DEFAULT 0,
                                     max_attempts INTEGER NOT NULL DEFAULT 5,
                                     verified_at TIMESTAMPTZ,
                                     consumed_at TIMESTAMPTZ,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_reset_user_id
    ON password_reset_otps(user_id);

CREATE INDEX idx_password_reset_expiry
    ON password_reset_otps(expires_at);
