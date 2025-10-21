-- Add DEVELOPER role for property developers
INSERT INTO roles (name, description, permissions, created_at, updated_at)
VALUES (
           'DEVELOPER',
           'Property developer role with access to manage properties and view applications',
           '{
             "users": ["read"],
             "properties": ["create","read","update","delete"],
             "kpr_applications": ["read"],
             "developers": ["read","update"]
           }'::json,
           NOW(),
           NOW()
       )
ON CONFLICT (name) DO NOTHING;
