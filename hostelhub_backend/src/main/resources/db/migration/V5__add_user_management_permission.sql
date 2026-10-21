
-- Add the permission required for account status management.
INSERT INTO permissions (name, description)
VALUES (
           'USER_MANAGE',
           'Activate, suspend, or disable user accounts'
       )
    ON CONFLICT (name) DO NOTHING;

-- Only SUPER_ADMIN receives this global management permission.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN'
  AND p.name = 'USER_MANAGE'
    ON CONFLICT (role_id, permission_id) DO NOTHING;
