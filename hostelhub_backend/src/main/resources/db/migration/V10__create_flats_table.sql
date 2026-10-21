
CREATE TABLE flats (
                       id BIGSERIAL PRIMARY KEY,
                       floor_id BIGINT NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       code VARCHAR(50) NOT NULL,
                       description VARCHAR(1000),
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_flats_floor
                           FOREIGN KEY (floor_id)
                               REFERENCES floors(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT uq_flats_floor_code
                           UNIQUE (floor_id, code),

                       CONSTRAINT chk_flats_status
                           CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_flats_floor_id
    ON flats(floor_id);

CREATE INDEX idx_flats_floor_status
    ON flats(floor_id, status);
