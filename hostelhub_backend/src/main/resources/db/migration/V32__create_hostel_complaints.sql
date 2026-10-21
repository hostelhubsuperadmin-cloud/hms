CREATE TABLE hostel_complaints (
                                   id BIGSERIAL PRIMARY KEY,
                                   hostel_id BIGINT NOT NULL REFERENCES hostels(id),
                                   hostler_id BIGINT NOT NULL REFERENCES users(id),
                                   subject VARCHAR(150) NOT NULL,
                                   category VARCHAR(30) NOT NULL,
                                   priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
                                   description VARCHAR(3000) NOT NULL,
                                   status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                   admin_note VARCHAR(2000),
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT chk_complaint_category CHECK (
                                       category IN (
                                                    'FOOD', 'ROOM', 'WATER', 'ELECTRICITY',
                                                    'CLEANLINESS', 'PLUMBING', 'OTHER'
                                           )
                                       ),

                                   CONSTRAINT chk_complaint_priority CHECK (
                                       priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')
                                       ),

                                   CONSTRAINT chk_complaint_status CHECK (
                                       status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED')
                                       )
);

CREATE INDEX idx_complaint_hostel_status
    ON hostel_complaints(hostel_id, status);

CREATE INDEX idx_complaint_hostler
    ON hostel_complaints(hostler_id, created_at DESC);