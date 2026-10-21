CREATE TABLE permissions (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(255)
);

CREATE TABLE role_permissions (
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,

                                  PRIMARY KEY (role_id, permission_id),

                                  CONSTRAINT fk_role_permissions_role
                                      FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,

                                  CONSTRAINT fk_role_permissions_permission
                                      FOREIGN KEY (permission_id)
                                          REFERENCES permissions(id) ON DELETE CASCADE
);

INSERT INTO permissions (name, description) VALUES
                                                ('USER_READ', 'Read permitted user information'),
                                                ('USER_UPDATE_SELF', 'Update own profile'),
                                                ('ROLE_MANAGE', 'Manage application roles'),
                                                ('PERMISSION_MANAGE', 'Manage permissions'),
                                                ('HOSTEL_MANAGE', 'Manage hostels'),
                                                ('HOSTEL_STRUCTURE_MANAGE', 'Manage hostel structure'),
                                                ('HOSTLER_MANAGE', 'Manage residents'),
                                                ('ALLOCATION_MANAGE', 'Manage bed allocations'),
                                                ('PAYMENT_READ', 'Read permitted payment information'),
                                                ('PAYMENT_MANAGE', 'Manage payments'),
                                                ('COMPLAINT_MANAGE', 'Manage complaints'),
                                                ('REPORT_READ', 'Read permitted reports'),
                                                ('AUDIT_READ', 'Read audit logs');

-- Initial role-permission mappings.
-- SUPER_ADMIN receives all permissions present at this stage.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'USER_READ', 'USER_UPDATE_SELF'
    )
WHERE r.name = 'USER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'USER_READ', 'USER_UPDATE_SELF'
    )
WHERE r.name IN ('HOSTLER', 'WARDEN', 'ACCOUNTANT', 'STAFF');