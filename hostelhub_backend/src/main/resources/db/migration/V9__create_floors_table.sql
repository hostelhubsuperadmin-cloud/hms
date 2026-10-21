
CREATE TABLE floors (
                        id BIGSERIAL PRIMARY KEY,
                        block_id BIGINT NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        floor_number INTEGER NOT NULL,
                        description VARCHAR(1000),
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_floors_block
                            FOREIGN KEY (block_id)
                                REFERENCES blocks(id)
                                ON DELETE RESTRICT,

                        CONSTRAINT uq_floors_block_number
                            UNIQUE (block_id, floor_number),

                        CONSTRAINT chk_floors_number
                            CHECK (floor_number >= 0),

                        CONSTRAINT chk_floors_status
                            CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_floors_block_id
    ON floors(block_id);

CREATE INDEX idx_floors_block_status
    ON floors(block_id, status);
