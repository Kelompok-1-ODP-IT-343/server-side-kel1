-- ADMIN
INSERT INTO roles (name, description, permissions, created_at, updated_at)
VALUES (
           'ADMIN',
           'Administrator role with full system access',
           '{
             "users": ["create","read","update","delete"],
             "roles": ["create","read","update","delete"],
             "properties": ["create","read","update","delete"],
             "kpr_applications": ["approve","reject","review","update"],
             "loans": ["create","read","update","delete"],
             "system_settings": ["manage"]
           }'::json,
           NOW(),
           NOW()
       )
ON CONFLICT (name) DO NOTHING;

-- USER
INSERT INTO roles (name, description, permissions, created_at, updated_at)
VALUES (
           'USER',
           'Default user role with limited access',
           '{
             "users": ["read","update"],
             "properties": ["read"],
             "kpr_applications": ["create","read"],
             "loans": ["read"]
           }'::json,
           NOW(),
           NOW()
       )
ON CONFLICT (name) DO NOTHING;
