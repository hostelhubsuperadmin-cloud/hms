ALTER TABLE payments
    ADD COLUMN gateway_order_id VARCHAR(100),
    ADD COLUMN gateway_payment_id VARCHAR(100),
    ADD COLUMN manual_reference VARCHAR(100),
    ADD COLUMN proof_file_key VARCHAR(100),
    ADD COLUMN proof_original_filename VARCHAR(255),
    ADD COLUMN proof_content_type VARCHAR(100),
    ADD COLUMN proof_uploaded_at TIMESTAMPTZ;

ALTER TABLE payments
    ADD CONSTRAINT uq_payments_gateway_order_id
        UNIQUE (gateway_order_id);

ALTER TABLE payments
    ADD CONSTRAINT uq_payments_gateway_payment_id
        UNIQUE (gateway_payment_id);

ALTER TABLE payments
ALTER COLUMN status TYPE VARCHAR(30);

ALTER TABLE payments
DROP CONSTRAINT IF EXISTS chk_payments_status;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_status
        CHECK (
            status IN (
                       'PENDING',
                       'SUBMITTED',
                       'PROCESSING',
                       'SUCCESS',
                       'FAILED',
                       'REJECTED',
                       'REFUNDED',
                       'RECONCILIATION_REQUIRED'
                )
            );

CREATE TABLE payment_webhook_events (
                                        event_id VARCHAR(150) PRIMARY KEY,
                                        event_type VARCHAR(100) NOT NULL,
                                        received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        processed_at TIMESTAMPTZ
);