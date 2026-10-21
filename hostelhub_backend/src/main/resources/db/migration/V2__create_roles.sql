CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255)
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

INSERT INTO roles (name, description) VALUES
                                          ('SUPER_ADMIN', 'Platform-wide administration'),
                                          ('HOSTEL_ADMIN', 'Administration of assigned hostels'),
                                          ('WARDEN', 'Resident and hostel operations'),
                                          ('ACCOUNTANT', 'Invoices and payments'),
                                          ('STAFF', 'Assigned operational tasks'),
                                          ('HOSTLER', 'Resident self-service'),
                                          ('USER', 'Public user access');