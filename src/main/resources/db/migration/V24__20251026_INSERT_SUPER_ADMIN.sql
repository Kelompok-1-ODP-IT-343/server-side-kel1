INSERT INTO users
(id, username, email, phone, password_hash, role_id, status, email_verified_at, phone_verified_at, last_login_at,
 failed_login_attempts, locked_until, created_at, updated_at, consent_at)
VALUES (999, 'super_admin', 'super.super.admin@yopmail.com', NULL,
        '$2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG', 1, 'ACTIVE'::public.user_status,
        '2025-10-26 12:30:01.846', NULL, NULL, 0, NULL, '2025-10-26 12:40:29.674', '2025-10-26 12:40:29.674',
        '2025-10-26 12:40:29.674');

INSERT INTO user_profiles
(user_id, full_name, nik, npwp, birth_date, birth_place, gender, marital_status, address, city, province,
 postal_code, occupation, company_name, monthly_income, work_experience, created_at, updated_at)
VALUES (999, 'Super Admin', NULL, NULL, '2002-05-05', 'Jakarta ', NULL, NULL, NULL, NULL, NULL, NULL, 'PNS', NULL,
        10000000.00, NULL, '2025-10-26 12:29:27.969', '2025-10-26 12:29:27.969');