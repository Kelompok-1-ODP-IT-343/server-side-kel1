DO $$
DECLARE
    v_user_id_approver INT;
    v_user_id_manager INT;
    v_role_approver INT;
BEGIN
    ----------------------------------------------------------
    -- Ambil ID dari role APPROVER yang sudah ada
    ----------------------------------------------------------
    SELECT id INTO v_role_approver FROM public.roles WHERE name = 'APPROVER' LIMIT 1;

    IF v_role_approver IS NULL THEN
        RAISE EXCEPTION 'Role APPROVER not found!';
    END IF;

    ----------------------------------------------------------
    -- 1️⃣ Insert Approver User
    ----------------------------------------------------------
    INSERT INTO public.users (
        username,
        email,
        phone,
        password_hash,
        role_id,
        status,
        email_verified_at,
        created_at,
        updated_at,
        consent_at
    )
    VALUES (
        'approver_user',
        'approver.user@yopmail.com',
        '081234567890',
        '$2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG',
        v_role_approver,
        'ACTIVE'::public.user_status,
        NOW(),
        NOW(),
        NOW(),
        NOW()
    )
    RETURNING id INTO v_user_id_approver;

    INSERT INTO public.user_profiles (
        user_id,
        full_name,
        nik,
        npwp,
        birth_date,
        birth_place,
        gender,
        marital_status,
        address,
        city,
        province,
        postal_code,
        occupation,
        company_name,
        monthly_income,
        work_experience,
        created_at,
        updated_at
    )
    VALUES (
        v_user_id_approver,
        'Approver User',
        '3201010505050001',
        '1234567890123456',
        '1995-05-05',
        'Jakarta',
        'MALE'::public.gender_type,
        'SINGLE'::public.marital_status_type,
        'Jl. Merdeka No.1',
        'Jakarta',
        'DKI Jakarta',
        '10110',
        'Pegawai Bank',
        'Bank Satu Atap',
        15000000.00,
        5,
        NOW(),
        NOW()
    );

    INSERT INTO public.branch_staff (
        user_id,
        branch_code,
        staff_id,
        "position",
        supervisor_id,
        is_active,
        start_date,
        end_date,
        created_at,
        updated_at
    )
    VALUES (
        v_user_id_approver,
        'BR001',
        'STF-APPROVER-001',
        'CREDIT_ANALYST'::public.staff_position,
        NULL,
        TRUE,
        CURRENT_DATE,
        NULL,
        NOW(),
        NOW()
    );

    ----------------------------------------------------------
    -- 2️⃣ Insert Branch Manager User (role sama APPROVER)
    ----------------------------------------------------------
    INSERT INTO public.users (
        username,
        email,
        phone,
        password_hash,
        role_id,
        status,
        email_verified_at,
        created_at,
        updated_at,
        consent_at
    )
    VALUES (
        'branch_manager',
        'manager.branch@yopmail.com',
        '081298765432',
        '$2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG',
        v_role_approver,
        'ACTIVE'::public.user_status,
        NOW(),
        NOW(),
        NOW(),
        NOW()
    )
    RETURNING id INTO v_user_id_manager;

    INSERT INTO public.user_profiles (
        user_id,
        full_name,
        nik,
        npwp,
        birth_date,
        birth_place,
        gender,
        marital_status,
        address,
        city,
        province,
        postal_code,
        occupation,
        company_name,
        monthly_income,
        work_experience,
        created_at,
        updated_at
    )
    VALUES (
        v_user_id_manager,
        'Branch Manager User',
        '3201020202020002',
        '6543210987654321',
        '1990-02-02',
        'Bandung',
        'MALE'::public.gender_type,
        'MARRIED'::public.marital_status_type,
        'Jl. Sudirman No.2',
        'Bandung',
        'Jawa Barat',
        '40123',
        'Manajer Cabang',
        'Bank Satu Atap',
        25000000.00,
        10,
        NOW(),
        NOW()
    );

    INSERT INTO public.branch_staff (
        user_id,
        branch_code,
        staff_id,
        "position",
        supervisor_id,
        is_active,
        start_date,
        end_date,
        created_at,
        updated_at
    )
    VALUES (
        v_user_id_manager,
        'BR001',
        'STF-MANAGER-001',
        'BRANCH_MANAGER'::public.staff_position,
        NULL,
        TRUE,
        CURRENT_DATE,
        NULL,
        NOW(),
        NOW()
    );
END $$;
