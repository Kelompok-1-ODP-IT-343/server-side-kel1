-- Add DEVELOPER role for property developers
INSERT INTO roles (name, description, permissions)
VALUES (
           'DEVELOPER',
           'Property developer role with access to manage properties and view applications',
           '{
             "users": ["read"],
             "properties": ["create","read","update","delete"],
             "kpr_applications": ["read"],
             "developers": ["read","update"]
           }'::json
       )
ON CONFLICT (name) DO NOTHING;
