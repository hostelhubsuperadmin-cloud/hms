
CREATE TABLE beds (
                      id BIGSERIAL PRIMARY KEY,
                      room_id BIGINT NOT NULL,
                      bed_number VARCHAR(50) NOT NULL,
                      name VARCHAR(100),
                      description VARCHAR(1000),
                      status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
                      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      CONSTRAINT fk_beds_room
                          FOREIGN KEY (room_id)
                              REFERENCES rooms(id)
                              ON DELETE RESTRICT,

                      CONSTRAINT uq_beds_room_number
                          UNIQUE (room_id, bed_number),

                      CONSTRAINT chk_beds_status
                          CHECK (
                              status IN (
                                         'AVAILABLE',
                                         'OCCUPIED',
                                         'MAINTENANCE',
                                         'INACTIVE'
                                  )
                              )
);

CREATE INDEX idx_beds_room_id
    ON beds(room_id);

CREATE INDEX idx_beds_room_status
    ON beds(room_id, status);
