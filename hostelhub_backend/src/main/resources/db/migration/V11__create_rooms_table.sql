
CREATE TABLE rooms (
                       id BIGSERIAL PRIMARY KEY,
                       floor_id BIGINT,
                       flat_id BIGINT,
                       room_number VARCHAR(50) NOT NULL,
                       name VARCHAR(100),
                       room_type VARCHAR(30) NOT NULL DEFAULT 'DOUBLE',
                       capacity INTEGER NOT NULL,
                       description VARCHAR(1000),
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_rooms_floor
                           FOREIGN KEY (floor_id)
                               REFERENCES floors(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT fk_rooms_flat
                           FOREIGN KEY (flat_id)
                               REFERENCES flats(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT chk_rooms_exactly_one_parent
                           CHECK (
                               (floor_id IS NOT NULL AND flat_id IS NULL)
                                   OR
                               (floor_id IS NULL AND flat_id IS NOT NULL)
                               ),

                       CONSTRAINT chk_rooms_capacity
                           CHECK (capacity > 0),

                       CONSTRAINT chk_rooms_type
                           CHECK (
                               room_type IN (
                                             'SINGLE', 'DOUBLE', 'TRIPLE',
                                             'DORMITORY', 'OTHER'
                                   )
                               ),

                       CONSTRAINT chk_rooms_status
                           CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX uq_rooms_floor_number
    ON rooms (floor_id, LOWER(room_number))
    WHERE floor_id IS NOT NULL;

CREATE UNIQUE INDEX uq_rooms_flat_number
    ON rooms (flat_id, LOWER(room_number))
    WHERE flat_id IS NOT NULL;

CREATE INDEX idx_rooms_floor_status
    ON rooms (floor_id, status);

CREATE INDEX idx_rooms_flat_status
    ON rooms (flat_id, status);
