CREATE TABLE hostler_profiles (
                                  user_id BIGINT PRIMARY KEY,
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
                                  notes VARCHAR(2000),

                                  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_hostler_profiles_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT fk_hostler_profiles_hostel
                                      FOREIGN KEY (hostel_id)
                                          REFERENCES hostels(id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT chk_hostler_profiles_status
                                      CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_hostler_profiles_hostel_status
    ON hostler_profiles(hostel_id, status);

CREATE INDEX idx_hostler_profiles_hostel_created
    ON hostler_profiles(hostel_id, created_at DESC);