INSERT INTO public.roles (name, description, permissions, created_at, updated_at)
VALUES (
  'APPROVER',
  'Role untuk melakukan approval dan pengelolaan pengajuan KPR',
  '{"users":["read"],"kpr_applications":["read","write","update"],"developers":["read","update"]}'::json,
  NOW(),
  NOW()
)
ON CONFLICT (name) DO NOTHING;
