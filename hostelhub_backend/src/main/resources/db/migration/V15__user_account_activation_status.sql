
-- Optional: V15__user_account_activation_status.sql
-- Run only if equivalent columns do not already exist.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS account_status
    VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE users
    ADD CONSTRAINT chk_users_account_status
        CHECK (
            account_status IN (
                               'PENDING_ACTIVATION',
                               'ACTIVE',
                               'DISABLED'
                )
            );
