
CREATE TABLE blocks (
                        id BIGSERIAL PRIMARY KEY,
                        hostel_id BIGINT NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        code VARCHAR(50) NOT NULL,
                        description VARCHAR(1000),
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_blocks_hostel
                            FOREIGN KEY (hostel_id)
                                REFERENCES hostels(id)
                                ON DELETE RESTRICT,

                        CONSTRAINT uq_blocks_hostel_code
                            UNIQUE (hostel_id, code),

                        CONSTRAINT chk_blocks_status
                            CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_blocks_hostel_id
    ON blocks(hostel_id);

CREATE INDEX idx_blocks_hostel_status
    ON blocks(hostel_id, status);
