
CREATE TABLE hostel_admin_assignments (
                                          id BIGSERIAL PRIMARY KEY,

                                          hostel_id BIGINT NOT NULL,
                                          user_id BIGINT NOT NULL,
                                          assigned_by BIGINT NOT NULL,

                                          assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                          CONSTRAINT fk_hostel_admin_assignment_hostel
                                              FOREIGN KEY (hostel_id)
                                                  REFERENCES hostels(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_hostel_admin_assignment_user
                                              FOREIGN KEY (user_id)
                                                  REFERENCES users(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_hostel_admin_assignment_assigned_by
                                              FOREIGN KEY (assigned_by)
                                                  REFERENCES users(id),

                                          CONSTRAINT uq_hostel_admin_assignment
                                              UNIQUE (hostel_id, user_id)
);

CREATE INDEX idx_hostel_admin_assignments_user
    ON hostel_admin_assignments(user_id);

CREATE INDEX idx_hostel_admin_assignments_hostel
    ON hostel_admin_assignments(hostel_id);
