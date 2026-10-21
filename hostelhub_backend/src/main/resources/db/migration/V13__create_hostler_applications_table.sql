
CREATE TABLE hostler_applications (
                                      id BIGSERIAL PRIMARY KEY,

                                      user_id BIGINT NOT NULL,
                                      hostel_id BIGINT NOT NULL,

                                      date_of_birth DATE,
                                      gender VARCHAR(30),

                                      guardian_name VARCHAR(150),
                                      guardian_phone VARCHAR(20),
                                      emergency_contact VARCHAR(20),

                                      address_line VARCHAR(255),
                                      city VARCHAR(100),
                                      state VARCHAR(100),
                                      postal_code VARCHAR(20),

                                      requested_move_in_date DATE,
                                      applicant_notes VARCHAR(2000),

                                      status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                      reviewed_by BIGINT,
                                      reviewed_at TIMESTAMPTZ,
                                      review_notes VARCHAR(2000),

                                      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_hostler_app_user
                                          FOREIGN KEY (user_id)
                                              REFERENCES users(id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT fk_hostler_app_hostel
                                          FOREIGN KEY (hostel_id)
                                              REFERENCES hostels(id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT fk_hostler_app_reviewer
                                          FOREIGN KEY (reviewed_by)
                                              REFERENCES users(id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT chk_hostler_app_status
                                          CHECK (
                                              status IN (
                                                         'PENDING',
                                                         'APPROVED',
                                                         'REJECTED',
                                                         'CANCELLED'
                                                  )
                                              )
);

CREATE INDEX idx_hostler_app_user
    ON hostler_applications(user_id, created_at DESC);

CREATE INDEX idx_hostler_app_hostel_status
    ON hostler_applications(hostel_id, status, created_at DESC);

CREATE UNIQUE INDEX uq_hostler_app_pending
    ON hostler_applications(user_id, hostel_id)
    WHERE status = 'PENDING';
