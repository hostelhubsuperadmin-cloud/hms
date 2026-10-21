CREATE TABLE hostel_staff_assignments (
                                          id BIGSERIAL PRIMARY KEY,
                                          hostel_id BIGINT NOT NULL,
                                          user_id BIGINT NOT NULL,
                                          assigned_by BIGINT NOT NULL,
                                          staff_role VARCHAR(20) NOT NULL,
                                          assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                          CONSTRAINT fk_hostel_staff_hostel
                                              FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,

                                          CONSTRAINT fk_hostel_staff_user
                                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                                          CONSTRAINT fk_hostel_staff_assigned_by
                                              FOREIGN KEY (assigned_by) REFERENCES users(id),

                                          CONSTRAINT chk_hostel_staff_role
                                              CHECK (staff_role IN ('WARDEN', 'STAFF')),

                                          CONSTRAINT uq_hostel_staff_hostel_user
                                              UNIQUE (hostel_id, user_id)
);

CREATE INDEX idx_hostel_staff_hostel
    ON hostel_staff_assignments(hostel_id);

CREATE INDEX idx_hostel_staff_user
    ON hostel_staff_assignments(user_id);