
-- V4: Complete the initial role-permission mappings.
-- Existing mappings are preserved; duplicate mappings are ignored.

-- SUPER_ADMIN: all currently defined permissions.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN'
    ON CONFLICT (role_id, permission_id) DO NOTHING;

-- HOSTEL_ADMIN: administration of assigned hostels.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'USER_READ',
                                          'USER_UPDATE_SELF',
                                          'HOSTEL_MANAGE',
                                          'HOSTEL_STRUCTURE_MANAGE',
                                          'HOSTLER_MANAGE',
                                          'ALLOCATION_MANAGE',
                                          'PAYMENT_READ',
                                          'PAYMENT_MANAGE',
                                          'COMPLAINT_MANAGE',
                                          'REPORT_READ'
    )
WHERE r.name = 'HOSTEL_ADMIN'
    ON CONFLICT (role_id, permission_id) DO NOTHING;

-- WARDEN: resident and hostel operations.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'USER_READ',
                                          'USER_UPDATE_SELF',
                                          'HOSTLER_MANAGE',
                                          'ALLOCATION_MANAGE',
                                          'COMPLAINT_MANAGE',
                                          'REPORT_READ'
    )
WHERE r.name = 'WARDEN'
    ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ACCOUNTANT: payment and reporting operations.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'USER_UPDATE_SELF',
                                          'PAYMENT_READ',
                                          'PAYMENT_MANAGE',
                                          'REPORT_READ'
    )
WHERE r.name = 'ACCOUNTANT'
    ON CONFLICT (role_id, permission_id) DO NOTHING;

-- STAFF: limited operational permissions.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'USER_UPDATE_SELF',
                                          'COMPLAINT_MANAGE'
    )
WHERE r.name = 'STAFF'
    ON CONFLICT (role_id, permission_id) DO NOTHING;
