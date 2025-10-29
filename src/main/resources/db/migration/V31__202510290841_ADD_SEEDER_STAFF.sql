-- =========================================
-- STEP 1: Insert users dan ambil ID mereka
-- =========================================
WITH inserted_users AS (
    INSERT INTO public.users (
        username, email, phone, password_hash, role_id, status,
        email_verified_at, phone_verified_at, last_login_at, failed_login_attempts,
        locked_until, created_at, updated_at, consent_at
    ) VALUES
    ('admin01', 'admin01@griya.id', '081234562290', '$2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG', 1, 'ACTIVE',
        NOW(), NOW(), NOW(), 0, NULL, NOW(), NOW(), NOW()),
    ('staff001', 'budi.staff@griya.id', '0812118765432', '$2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG', 4, 'ACTIVE',
        NOW(), NOW(), NOW(), 0, NULL, NOW(), NOW(), NOW()),
    ('staff002', 'sari.staff@griya.id', '081211662233', '$2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG', 4, 'ACTIVE',
        NOW(), NOW(), NOW(), 0, NULL, NOW(), NOW(), NOW()),
    ('user001', 'andi.user@griya.id', '0812555986677', '$2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG', 2, 'PENDING_VERIFICATION',
        NULL, NULL, NULL, 0, NULL, NOW(), NOW(), NOW())
    RETURNING id, username
)
SELECT * INTO TEMP TABLE tmp_users FROM inserted_users;

-- =========================================
-- STEP 2: Insert branch_staff
-- (gunakan data dari tmp_users)
-- =========================================
INSERT INTO public.branch_staff (
    user_id, branch_code, staff_id, "position", supervisor_id, is_active, start_date, end_date, created_at, updated_at
)
VALUES (
    (SELECT id FROM tmp_users WHERE username = 'staff001'),
    'BR001', 'STF-001', 'BRANCH_MANAGER', NULL, true, '2022-01-10', NULL, NOW(), NOW()
);

-- Dapatkan ID supervisor untuk assign ke staf kedua
DO $$
DECLARE
    v_supervisor_id INT;
BEGIN
    SELECT id INTO v_supervisor_id FROM public.branch_staff WHERE staff_id = 'STF-001';

    INSERT INTO public.branch_staff (
        user_id, branch_code, staff_id, "position", supervisor_id, is_active, start_date, end_date, created_at, updated_at
    ) VALUES (
        (SELECT id FROM tmp_users WHERE username = 'staff002'),
        'BR001', 'STF-002', 'CREDIT_ANALYST', v_supervisor_id, true, '2022-03-15', NULL, NOW(), NOW()
    );
END $$;

-- =========================================
-- STEP 3: Insert user_profiles
-- =========================================
INSERT INTO public.user_profiles (
    user_id, full_name, nik, npwp, birth_date, birth_place, gender, marital_status,
    address, city, province, postal_code, occupation, company_name,
    monthly_income, work_experience, created_at, updated_at
)
SELECT
    id AS user_id,
    CASE username
        WHEN 'admin01' THEN 'Admin Utama'
        WHEN 'staff001' THEN 'Budi Santoso'
        WHEN 'staff002' THEN 'Sari Lestari'
        WHEN 'user001' THEN 'Andi Pratama'
    END AS full_name,
    CASE username
        WHEN 'admin01' THEN '3201010101010001'
        WHEN 'staff001' THEN '3202020202020002'
        WHEN 'staff002' THEN '3203030303030003'
        WHEN 'user001' THEN '3204040404040004'
    END AS nik,
    CASE username
        WHEN 'admin01' THEN '1234562290123456'
        WHEN 'staff001' THEN '6543266987654321'
        WHEN 'staff002' THEN '7894577230123456'
        WHEN 'user001' THEN '3456788812345678'
    END AS npwp,
    CASE username
        WHEN 'admin01' THEN '1985-05-10'::date
        WHEN 'staff001' THEN '1990-07-15'::date
        WHEN 'staff002' THEN '1995-08-21'::date
        WHEN 'user001' THEN '1998-11-30'::date
    END AS birth_date,
    CASE username
        WHEN 'admin01' THEN 'Jakarta'
        WHEN 'staff001' THEN 'Bandung'
        WHEN 'staff002' THEN 'Surabaya'
        WHEN 'user001' THEN 'Yogyakarta'
    END AS birth_place,
    CASE username
        WHEN 'admin01' THEN 'MALE'
        WHEN 'staff001' THEN 'MALE'
        WHEN 'staff002' THEN 'FEMALE'
        WHEN 'user001' THEN 'MALE'
    END::public.gender_type AS gender,
    CASE username
        WHEN 'admin01' THEN 'MARRIED'
        WHEN 'staff001' THEN 'MARRIED'
        WHEN 'staff002' THEN 'SINGLE'
        WHEN 'user001' THEN 'SINGLE'
    END::public.marital_status_type AS marital_status,
    CASE username
        WHEN 'admin01' THEN 'Jl. Merdeka No.1'
        WHEN 'staff001' THEN 'Jl. Cihampelas No.5'
        WHEN 'staff002' THEN 'Jl. Raya Darmo No.10'
        WHEN 'user001' THEN 'Jl. Malioboro No.20'
    END AS address,
    CASE username
        WHEN 'admin01' THEN 'Jakarta Pusat'
        WHEN 'staff001' THEN 'Bandung'
        WHEN 'staff002' THEN 'Surabaya'
        WHEN 'user001' THEN 'Yogyakarta'
    END AS city,
    CASE username
        WHEN 'admin01' THEN 'DKI Jakarta'
        WHEN 'staff001' THEN 'Jawa Barat'
        WHEN 'staff002' THEN 'Jawa Timur'
        WHEN 'user001' THEN 'DI Yogyakarta'
    END AS province,
    CASE username
        WHEN 'admin01' THEN '10110'
        WHEN 'staff001' THEN '40131'
        WHEN 'staff002' THEN '60241'
        WHEN 'user001' THEN '55213'
    END AS postal_code,
    CASE username
        WHEN 'admin01' THEN 'Administrator'
        WHEN 'staff001' THEN 'Branch Manager'
        WHEN 'staff002' THEN 'Customer Service'
        WHEN 'user001' THEN 'Sales Executive'
    END AS occupation,
    'PT Griya Digital' AS company_name,
    CASE username
        WHEN 'admin01' THEN 15000000.00
        WHEN 'staff001' THEN 12000000.00
        WHEN 'staff002' THEN 8000000.00
        WHEN 'user001' THEN 6000000.00
    END AS monthly_income,
    CASE username
        WHEN 'admin01' THEN 10
        WHEN 'staff001' THEN 8
        WHEN 'staff002' THEN 4
        WHEN 'user001' THEN 2
    END AS work_experience,
    NOW(), NOW()
FROM tmp_users;

-- Optional: hapus tabel temp biar bersih
DROP TABLE tmp_users;
