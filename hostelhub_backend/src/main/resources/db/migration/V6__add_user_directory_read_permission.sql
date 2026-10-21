
INSERT INTO permissions (name, description)
VALUES (
           'USER_DIRECTORY_READ',
           'View the global user directory and user details'
       )
    ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN'
  AND p.name = 'USER_DIRECTORY_READ'
    ON CONFLICT (role_id, permission_id) DO NOTHING;
