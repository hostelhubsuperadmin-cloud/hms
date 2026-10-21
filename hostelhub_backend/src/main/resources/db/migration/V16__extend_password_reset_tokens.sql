
-- V16__extend_password_reset_tokens.sql

ALTER TABLE password_reset_otps
    ADD COLUMN reset_token_hash VARCHAR(128);

ALTER TABLE password_reset_otps
    ADD COLUMN reset_token_expires_at TIMESTAMPTZ;

ALTER TABLE password_reset_otps
    ADD CONSTRAINT chk_password_reset_attempts
        CHECK (attempts >= 0 AND max_attempts > 0);

CREATE UNIQUE INDEX uq_password_reset_reset_token_hash
    ON password_reset_otps(reset_token_hash)
    WHERE reset_token_hash IS NOT NULL;

