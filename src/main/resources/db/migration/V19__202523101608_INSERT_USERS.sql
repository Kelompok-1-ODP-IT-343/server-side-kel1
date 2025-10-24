INSERT INTO public.users (
    username, email, phone, password_hash, role_id, status,
    email_verified_at, phone_verified_at, last_login_at,
    failed_login_attempts, locked_until, created_at, updated_at, consent_at
) VALUES
      ('dev_bumi', 'dev.bumi@bumiasri.co.id', '081211112233', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_sentosa', 'dev.sentosa@majuproperti.id', '081233344455', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_cipta', 'dev.cipta@cipta-nusantara.com', '081255566677', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_griya', 'dev.griya@griyamandiri.co.id', '081277788899', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_citra', 'dev.citra@citraalam.id', '081299900011', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_puncak', 'dev.puncak@puncaklestari.com', '081322233344', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_bsd', 'dev.bsd@bsdcentral.co.id', '081344455566', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_harmony', 'dev.harmony@harmonyestate.com', '081366677788', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_nusantara', 'dev.nusantara@nusantaraproperti.id', '081388899900', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW()),
      ('dev_mega', 'dev.mega@megagrahasentosa.com', '081300011122', 'hashed_password', 3, 'ACTIVE'::user_status, NOW(), NOW(), NULL, 0, NULL, NOW(), NOW(), NOW())
    on conflict (username) do nothing
