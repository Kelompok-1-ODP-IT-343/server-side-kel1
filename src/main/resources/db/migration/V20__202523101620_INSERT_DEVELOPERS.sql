INSERT INTO developers (
    user_id, company_name, company_code, business_license, developer_license,
    contact_person, phone, email, website,
    address, city, province, postal_code, established_year,
    description, specialization, is_partner, partnership_level,
    commission_rate, status, verified_at, created_at, updated_at
) VALUES
      (
          (SELECT id FROM users WHERE username = 'dev_bumi'),
          'PT Bumi Asri Sejahtera', 'DEV-001', 'BL-11223', 'DL-55667', 'Adi Pranata',
          '0215551234', 'info@bumiasri.co.id', 'www.bumiasri.co.id',
          'Jl. Raya Bogor No.45', 'Bogor', 'Jawa Barat', '16115', 2010,
          'Fokus pada pengembangan perumahan menengah ke atas dengan konsep hijau.',
          'RESIDENTIAL', true, 'GOLD', 0.0250, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_sentosa'),
          'PT Maju Sentosa Properti', 'DEV-002', 'BL-22334', 'DL-66778', 'Budi Santoso',
          '0216662345', 'contact@majuproperti.id', 'www.majuproperti.id',
          'Jl. Sudirman No.88', 'Jakarta Pusat', 'DKI Jakarta', '10220', 2012,
          'Pengembang kawasan hunian vertikal dan apartemen modern.',
          'COMMERCIAL', true, 'PLATINUM', 0.0350, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_cipta'),
          'PT Cipta Karya Nusantara', 'DEV-003', 'BL-33445', 'DL-77889', 'Dewi Lestari',
          '0217773456', 'support@cipta-nusantara.com', 'www.cipta-nusantara.com',
          'Jl. Diponegoro No.12', 'Bandung', 'Jawa Barat', '40132', 2015,
          'Menyediakan hunian modern dan ramah lingkungan.',
          'RESIDENTIAL', true, 'SILVER', 0.0200, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_griya'),
          'PT Griya Mandiri Abadi', 'DEV-004', 'BL-88990', 'DL-33221', 'Rahmat Hidayat',
          '0218889990', 'info@griyamandiri.co.id', 'www.griyamandiri.co.id',
          'Jl. Raya Cinere No.25', 'Depok', 'Jawa Barat', '16514', 2018,
          'Pengembang perumahan modern dan townhouse di wilayah Depok dan Cinere.',
          'RESIDENTIAL', true, 'GOLD', 0.0250, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_citra'),
          'PT Citra Alam Sejahtera', 'DEV-005', 'BL-99887', 'DL-44556', 'Dewi Kartika',
          '0217774444', 'marketing@citraalam.id', 'www.citraalam.id',
          'Jl. Margonda Raya No.120', 'Depok', 'Jawa Barat', '16424', 2016,
          'Fokus pada pengembangan hunian hijau ramah lingkungan.',
          'RESIDENTIAL', true, 'SILVER', 0.0200, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_puncak'),
          'PT Puncak Indah Lestari', 'DEV-006', 'BL-66778', 'DL-77889', 'Yusuf Pranoto',
          '0251755333', 'cs@puncaklestari.com', 'www.puncaklestari.com',
          'Jl. Raya Puncak KM 80', 'Bogor', 'Jawa Barat', '16770', 2008,
          'Spesialis villa dan resort mewah di area Puncak dan Bogor.',
          'RESIDENTIAL', true, 'GOLD', 0.0300, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_bsd'),
          'PT BSD Central Property', 'DEV-007', 'BL-22334', 'DL-55667', 'Ari Gunawan',
          '0219995555', 'info@bsdcentral.co.id', 'www.bsdcentral.co.id',
          'Jl. BSD Boulevard No.5', 'Tangerang Selatan', 'Banten', '15345', 2014,
          'Pengembang komersial dan ruko di kawasan BSD City.',
          'COMMERCIAL', true, 'PLATINUM', 0.0350, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_harmony'),
          'PT Harmony Estate', 'DEV-008', 'BL-33445', 'DL-66778', 'Wahyu Lestari',
          '0215556666', 'admin@harmonyestate.com', 'www.harmonyestate.com',
          'Jl. Boulevard Barat No.88', 'Bekasi', 'Jawa Barat', '17116', 2019,
          'Membangun rumah keluarga menengah ke atas dengan konsep tropis modern.',
          'RESIDENTIAL', true, 'SILVER', 0.0200, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_nusantara'),
          'PT Nusantara Properti Prima', 'DEV-009', 'BL-44556', 'DL-77889', 'Agus Setiawan',
          '0213334444', 'sales@nusantaraproperti.id', 'www.nusantaraproperti.id',
          'Jl. Gatot Subroto No.20', 'Jakarta Selatan', 'DKI Jakarta', '12210', 2011,
          'Pengembang proyek properti terpadu dan mixed-use development.',
          'MIXED', true, 'GOLD', 0.0250, 'ACTIVE', NOW(), NOW(), NOW()
      ),
      (
          (SELECT id FROM users WHERE username = 'dev_mega'),
          'PT Mega Graha Sentosa', 'DEV-010', 'BL-55667', 'DL-88990', 'Lina Sari',
          '0216667777', 'info@megagrahasentosa.com', 'www.megagrahasentosa.com',
          'Jl. Daan Mogot No.55', 'Jakarta Barat', 'DKI Jakarta', '11460', 2013,
          'Fokus pada pengembangan apartemen dan area komersial di Jakarta Barat.',
          'COMMERCIAL', true, 'GOLD', 0.0280, 'ACTIVE', NOW(), NOW(), NOW()
      )
    on conflict (company_code) do nothing