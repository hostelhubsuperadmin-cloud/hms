CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       full_name VARCHAR(120) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       phone VARCHAR(30),
                       status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT chk_users_status
                           CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DISABLED'))
);

CREATE INDEX idx_users_status ON users(status);