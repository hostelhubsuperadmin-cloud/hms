
-- V17__align_user_account_status.sql

-- Remove the old status constraint first so PENDING_ACTIVATION is allowed.
ALTER TABLE users
DROP CONSTRAINT IF EXISTS chk_users_status;

-- Preserve valid account status values.
UPDATE users
SET status = CASE
                 WHEN account_status = 'PENDING_ACTIVATION' THEN 'PENDING_ACTIVATION'
                 WHEN account_status = 'ACTIVE' THEN 'ACTIVE'
                 WHEN account_status = 'SUSPENDED' THEN 'SUSPENDED'
                 WHEN account_status = 'DISABLED' THEN 'DISABLED'
                 ELSE status
    END
WHERE account_status IN (
                         'PENDING_ACTIVATION',
                         'ACTIVE',
                         'SUSPENDED',
                         'DISABLED'
    );

-- Apply the status values used by the application.
ALTER TABLE users
    ADD CONSTRAINT chk_users_status
        CHECK (
            status IN (
                       'PENDING',
                       'PENDING_ACTIVATION',
                       'ACTIVE',
                       'SUSPENDED',
                       'DISABLED'
                )
            );

-- Remove the temporary/legacy column after migration.
ALTER TABLE users
DROP CONSTRAINT IF EXISTS chk_users_account_status;

ALTER TABLE users
DROP COLUMN IF EXISTS account_status;



