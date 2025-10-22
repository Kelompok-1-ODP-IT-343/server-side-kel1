-- Description: Seed data for developers table
INSERT INTO developers (
    company_name, company_code, business_license, developer_license, contact_person, phone, email, website,
    address, city, province, postal_code, established_year, description, specialization, is_partner,
    partnership_level, commission_rate, status, verified_at, created_at, updated_at
) VALUES
      ('PT Bumi Asri Sejahtera', 'DEV-001', 'BL-11223', 'DL-55667', 'Adi Pranata', '0215551234', 'info@bumiasri.co.id', 'www.bumiasri.co.id',
       'Jl. Raya Bogor No.45', 'Bogor', 'Jawa Barat', '16115', 2010, 'Fokus pada pengembangan perumahan menengah ke atas dengan konsep hijau.', 'residential', true, 'gold', 0.0250, 'active', NOW(), NOW(), NOW()),

      ('PT Maju Sentosa Properti', 'DEV-002', 'BL-22334', 'DL-66778', 'Budi Santoso', '0216662345', 'contact@majuproperti.id', 'www.majuproperti.id',
       'Jl. Sudirman No.88', 'Jakarta Pusat', 'DKI Jakarta', '10220', 2012, 'Pengembang kawasan hunian vertikal dan apartemen modern.', 'commercial', true, 'platinum', 0.0350, 'active', NOW(), NOW(), NOW()),

      ('PT Cipta Karya Nusantara', 'DEV-003', 'BL-33445', 'DL-77889', 'Dewi Lestari', '0217773456', 'support@cipta-nusantara.com', 'www.cipta-nusantara.com',
       'Jl. Diponegoro No.12', 'Bandung', 'Jawa Barat', '40132', 2015, 'Menyediakan hunian modern dan ramah lingkungan.', 'residential', true, 'silver', 0.0200, 'active', NOW(), NOW(), NOW()),

      ('PT Griya Mandiri Abadi', 'DEV-004', 'BL-88990', 'DL-33221', 'Rahmat Hidayat', '0218889990', 'info@griyamandiri.co.id', 'www.griyamandiri.co.id',
       'Jl. Raya Cinere No.25', 'Depok', 'Jawa Barat', '16514', 2018, 'Pengembang perumahan modern dan townhouse di wilayah Depok dan Cinere.', 'residential', true, 'gold', 0.0250, 'active', NOW(), NOW(), NOW()),

      ('PT Citra Alam Sejahtera', 'DEV-005', 'BL-99887', 'DL-44556', 'Dewi Kartika', '0217774444', 'marketing@citraalam.id', 'www.citraalam.id',
       'Jl. Margonda Raya No.120', 'Depok', 'Jawa Barat', '16424', 2016, 'Fokus pada pengembangan hunian hijau ramah lingkungan.', 'residential', true, 'silver', 0.0200, 'active', NOW(), NOW(), NOW()),

      ('PT Puncak Indah Lestari', 'DEV-006', 'BL-66778', 'DL-77889', 'Yusuf Pranoto', '0251755333', 'cs@puncaklestari.com', 'www.puncaklestari.com',
       'Jl. Raya Puncak KM 80', 'Bogor', 'Jawa Barat', '16770', 2008, 'Spesialis villa dan resort mewah di area Puncak dan Bogor.', 'residential', true, 'gold', 0.0300, 'active', NOW(), NOW(), NOW()),

      ('PT BSD Central Property', 'DEV-007', 'BL-22334', 'DL-55667', 'Ari Gunawan', '0219995555', 'info@bsdcentral.co.id', 'www.bsdcentral.co.id',
       'Jl. BSD Boulevard No.5', 'Tangerang Selatan', 'Banten', '15345', 2014, 'Pengembang komersial dan ruko di kawasan BSD City.', 'commercial', true, 'platinum', 0.0350, 'active', NOW(), NOW(), NOW()),

      ('PT Harmony Estate', 'DEV-008', 'BL-33445', 'DL-66778', 'Wahyu Lestari', '0215556666', 'admin@harmonyestate.com', 'www.harmonyestate.com',
       'Jl. Boulevard Barat No.88', 'Bekasi', 'Jawa Barat', '17116', 2019, 'Membangun rumah keluarga menengah ke atas dengan konsep tropis modern.', 'residential', true, 'silver', 0.0200, 'active', NOW(), NOW(), NOW()),

      ('PT Nusantara Properti Prima', 'DEV-009', 'BL-44556', 'DL-77889', 'Agus Setiawan', '0213334444', 'sales@nusantaraproperti.id', 'www.nusantaraproperti.id',
       'Jl. Gatot Subroto No.20', 'Jakarta Selatan', 'DKI Jakarta', '12210', 2011, 'Pengembang proyek properti terpadu dan mixed-use development.', 'mixed', true, 'gold', 0.0250, 'active', NOW(), NOW(), NOW()),

      ('PT Mega Graha Sentosa', 'DEV-010', 'BL-55667', 'DL-88990', 'Lina Sari', '0216667777', 'info@megagrahasentosa.com', 'www.megagrahasentosa.com',
       'Jl. Daan Mogot No.55', 'Jakarta Barat', 'DKI Jakarta', '11460', 2013, 'Fokus pada pengembangan apartemen dan area komersial di Jakarta Barat.', 'commercial', true, 'gold', 0.0280, 'active', NOW(), NOW(), NOW()),

      ('PT Artha Properti Sejahtera', 'DEV-011', 'BL-66700', 'DL-11001', 'Sinta Wulandari', '0217770001', 'contact@arthaproperti.id', 'www.arthaproperti.id',
       'Jl. Raya Serpong No.18', 'Tangerang', 'Banten', '15311', 2012, 'Pengembang hunian vertikal dan rumah susun modern di kawasan Serpong.', 'residential', true, 'gold', 0.0250, 'active', NOW(), NOW(), NOW()),

      ('PT Gading Raya Properti', 'DEV-012', 'BL-66881', 'DL-12002', 'Teguh Santoso', '0218882233', 'sales@gadingraya.com', 'www.gadingraya.com',
       'Jl. Boulevard Kelapa Gading No.2', 'Jakarta Utara', 'DKI Jakarta', '14240', 2010, 'Spesialis properti komersial dan apartemen di Jakarta Utara.', 'commercial', true, 'silver', 0.0200, 'active', NOW(), NOW(), NOW()),

      ('PT Sentra Karya Makmur', 'DEV-013', 'BL-77882', 'DL-13003', 'Hendra Wijaya', '0227788990', 'info@skm.co.id', 'www.skm.co.id',
       'Jl. Soekarno Hatta No.190', 'Bandung', 'Jawa Barat', '40286', 2015, 'Fokus pada pengembangan kawasan industri dan pergudangan.', 'industrial', true, 'gold', 0.0270, 'active', NOW(), NOW(), NOW()),

      ('PT Mitra Land Development', 'DEV-014', 'BL-88993', 'DL-14004', 'Ayu Fitriani', '0317774455', 'info@mitraland.id', 'www.mitraland.id',
       'Jl. Mayjend Sungkono No.30', 'Surabaya', 'Jawa Timur', '60225', 2017, 'Pengembang proyek hunian dan ruko di Surabaya Barat.', 'mixed', true, 'silver', 0.0220, 'active', NOW(), NOW(), NOW()),

      ('PT Daya Cipta Properti', 'DEV-015', 'BL-99004', 'DL-15005', 'Indra Saputra', '0274888777', 'support@dayacipta.co.id', 'www.dayacipta.co.id',
       'Jl. Solo Baru No.88', 'Sukoharjo', 'Jawa Tengah', '57552', 2014, 'Mengembangkan perumahan subsidi dan non-subsidi di Jawa Tengah.', 'residential', true, 'bronze', 0.0180, 'active', NOW(), NOW(), NOW()),

      ('PT Samudra Vista Land', 'DEV-016', 'BL-33448', 'DL-16006', 'Vina Marlina', '0361223344', 'hello@samudravista.id', 'www.samudravista.id',
       'Jl. Sunset Road No.77', 'Denpasar', 'Bali', '80361', 2013, 'Pengembang villa dan properti wisata di Bali.', 'resort', true, 'gold', 0.0300, 'active', NOW(), NOW(), NOW()),

      ('PT Cakra Bumi Properti', 'DEV-017', 'BL-44559', 'DL-17007', 'Dimas Prakoso', '0248899333', 'marketing@cakrabumi.com', 'www.cakrabumi.com',
       'Jl. Pandanaran No.45', 'Semarang', 'Jawa Tengah', '50134', 2018, 'Spesialis pengembangan perumahan minimalis modern.', 'residential', true, 'silver', 0.0210, 'active', NOW(), NOW(), NOW()),

      ('PT Makmur Jaya Realty', 'DEV-018', 'BL-55660', 'DL-18008', 'Ratna Dewi', '0617771122', 'admin@makmurjaya.id', 'www.makmurjaya.id',
       'Jl. Gatot Subroto No.55', 'Medan', 'Sumatera Utara', '20122', 2011, 'Pengembang perumahan dan kawasan komersial di Medan.', 'mixed', true, 'gold', 0.0260, 'active', NOW(), NOW(), NOW()),

      ('PT Sahabat Properti Nusantara', 'DEV-019', 'BL-66771', 'DL-19009', 'Bayu Ramadhan', '0318899000', 'cs@sahabatproperti.id', 'www.sahabatproperti.id',
       'Jl. Diponegoro No.20', 'Surabaya', 'Jawa Timur', '60241', 2019, 'Fokus pada pembangunan rumah menengah dengan konsep smart home.', 'residential', true, 'silver', 0.0200, 'active', NOW(), NOW(), NOW()),

      ('PT Prima Land Abadi', 'DEV-020', 'BL-77882', 'DL-20010', 'Erika Ningsih', '0213344556', 'info@primaland.co.id', 'www.primaland.co.id',
       'Jl. Rasuna Said No.60', 'Jakarta Selatan', 'DKI Jakarta', '12950', 2015, 'Pengembang proyek apartemen dan co-living space di Jakarta Selatan.', 'commercial', true, 'gold', 0.0280, 'active', NOW(), NOW(), NOW());

-- End of V17__202522101006_DATA_SEEDS_PROPERTIES.sql

-- Description: Seed data for property_images table
INSERT INTO property_images (
    property_id, image_type, image_category, file_name, file_path, file_size,
    mime_type, width, height, alt_text, caption, sort_order, is_primary, uploaded_at
) VALUES
-- Property 1
(1, 'exterior', 'main', '01.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/01.jpg', 512000, 'image/jpeg', 1920, 1080, 'Tampak depan rumah Bogor', 'Rumah cluster hijau di Bogor', 1, true, NOW()),
(1, 'interior', 'gallery', '02.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/02.jpg', 465000, 'image/jpeg', 1920, 1080, 'Ruang tamu rumah Bogor', 'Ruang tamu lega dengan pencahayaan alami', 2, false, NOW()),

-- Property 2
(2, 'exterior', 'main', '03.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/03.jpg', 480000, 'image/jpeg', 1920, 1080, 'Apartemen Jakarta tampak depan', 'Gedung apartemen modern di Sudirman', 1, true, NOW()),
(2, 'interior', 'gallery', '04.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/04.jpg', 420000, 'image/jpeg', 1920, 1080, 'Kamar apartemen Jakarta', 'Desain interior minimalis', 2, false, NOW()),

-- Property 3
(3, 'exterior', 'main', '05.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/05.jpg', 490000, 'image/jpeg', 1920, 1080, 'Perumahan Bandung tampak depan', 'Cluster asri di Bandung Timur', 1, true, NOW()),
(3, 'interior', 'gallery', '06.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/06.jpg', 450000, 'image/jpeg', 1920, 1080, 'Kamar tidur utama Bandung', 'Interior bergaya Skandinavia', 2, false, NOW()),

-- Property 4
(4, 'exterior', 'main', '07.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/07.jpg', 510000, 'image/jpeg', 1920, 1080, 'Tampak depan rumah Depok', 'Rumah 2 lantai di Cinere', 1, true, NOW()),
(4, 'interior', 'gallery', '08.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/08.jpg', 430000, 'image/jpeg', 1920, 1080, 'Ruang keluarga rumah Depok', 'Desain open space modern', 2, false, NOW()),

-- Property 5
(5, 'exterior', 'main', '09.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/09.jpg', 495000, 'image/jpeg', 1920, 1080, 'Perumahan hijau Depok', 'Hunian ramah lingkungan di Margonda', 1, true, NOW()),
(5, 'interior', 'gallery', '10.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/10.jpg', 400000, 'image/jpeg', 1920, 1080, 'Dapur rumah hijau Depok', 'Dapur modern dengan material eco-friendly', 2, false, NOW()),

-- Property 6
(6, 'exterior', 'main', '11.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/11.jpg', 530000, 'image/jpeg', 1920, 1080, 'Villa Puncak tampak depan', 'Villa mewah dengan pemandangan gunung', 1, true, NOW()),
(6, 'interior', 'gallery', '12.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/12.jpg', 460000, 'image/jpeg', 1920, 1080, 'Ruang tamu villa Puncak', 'Interior hangat dengan perapian', 2, false, NOW()),

-- Property 7
(7, 'exterior', 'main', '13.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/13.jpg', 500000, 'image/jpeg', 1920, 1080, 'Ruko BSD tampak depan', 'Ruko 3 lantai area komersial BSD', 1, true, NOW()),
(7, 'interior', 'gallery', '14.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/14.jpg', 440000, 'image/jpeg', 1920, 1080, 'Interior ruko BSD', 'Ruang usaha siap pakai', 2, false, NOW()),

-- Property 8
(8, 'exterior', 'main', '15.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/15.jpg', 480000, 'image/jpeg', 1920, 1080, 'Townhouse Bekasi tampak depan', 'Townhouse modern dengan carport', 1, true, NOW()),
(8, 'interior', 'gallery', '16.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/16.jpg', 420000, 'image/jpeg', 1920, 1080, 'Ruang makan townhouse Bekasi', 'Konsep open kitchen modern', 2, false, NOW()),

-- Property 9
(9, 'exterior', 'main', '17.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/17.jpg', 510000, 'image/jpeg', 1920, 1080, 'Apartemen Gatot Subroto tampak depan', 'Gedung apartemen mewah di Jakarta', 1, true, NOW()),
(9, 'interior', 'gallery', '18.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/18.jpg', 460000, 'image/jpeg', 1920, 1080, 'Kamar apartemen Gatot Subroto', 'Desain premium dengan balkon view kota', 2, false, NOW()),

-- Property 10
(10, 'exterior', 'main', '19.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/19.jpg', 550000, 'image/jpeg', 1920, 1080, 'Ruko Daan Mogot tampak depan', 'Ruko komersial strategis di Jakarta Barat', 1, true, NOW()),
(10, 'interior', 'gallery', '20.jpg', 'https://is3.cloudhost.id/griyabni01/Sample01/20.jpg', 470000, 'image/jpeg', 1920, 1080, 'Interior ruko Daan Mogot', 'Ruang usaha 2 lantai siap pakai', 2, false, NOW());

-- End of V17__202522101006_DATA_SEEDS_PROPERTIES.sql

-- Description: Seed data for property_features table
INSERT INTO property_features (
    property_id, feature_category, feature_name, feature_value, is_highlight, created_at
) VALUES
-- Property 1 - Bogor
(1, 'interior', 'Kamar Tidur', '3', true, NOW()),
(1, 'interior', 'Kamar Mandi', '2', true, NOW()),
(1, 'exterior', 'Garasi', '1 Mobil', false, NOW()),
(1, 'utilities', 'Listrik', '2200 Watt', false, NOW()),
(1, 'security', 'CCTV', '24 Jam', false, NOW()),

-- Property 2 - Jakarta Pusat
(2, 'interior', 'Kamar Tidur', '2', true, NOW()),
(2, 'interior', 'Kamar Mandi', '1', true, NOW()),
(2, 'amenities', 'Kolam Renang', 'Umum', false, NOW()),
(2, 'utilities', 'AC', '2 Unit', false, NOW()),
(2, 'security', 'Akses Kartu', 'Ada', false, NOW()),

-- Property 3 - Bandung
(3, 'interior', 'Kamar Tidur', '3', true, NOW()),
(3, 'interior', 'Kamar Mandi', '2', true, NOW()),
(3, 'exterior', 'Carport', '1 Mobil', false, NOW()),
(3, 'utilities', 'Air', 'PDAM', false, NOW()),
(3, 'security', 'CCTV', 'Ada', false, NOW()),

-- Property 4 - Depok (Cinere)
(4, 'interior', 'Kamar Tidur', '3', true, NOW()),
(4, 'interior', 'Kamar Mandi', '3', true, NOW()),
(4, 'exterior', 'Garasi', '1 Mobil', false, NOW()),
(4, 'utilities', 'Listrik', '2200 Watt', false, NOW()),
(4, 'security', 'CCTV', '24 Jam', false, NOW()),

-- Property 5 - Depok (Margonda)
(5, 'interior', 'Kamar Tidur', '2', true, NOW()),
(5, 'interior', 'Kamar Mandi', '2', true, NOW()),
(5, 'amenities', 'Taman', 'Ada', false, NOW()),
(5, 'utilities', 'Air', 'PDAM', false, NOW()),
(5, 'security', 'Akses Gerbang', 'Smart Gate', false, NOW()),

-- Property 6 - Bogor (Villa)
(6, 'interior', 'Kamar Tidur', '4', true, NOW()),
(6, 'interior', 'Kamar Mandi', '3', true, NOW()),
(6, 'amenities', 'Kolam Renang', 'Private', true, NOW()),
(6, 'utilities', 'Pemanas Air', 'Ada', false, NOW()),
(6, 'exterior', 'Balkon', '2', false, NOW()),

-- Property 7 - Tangerang Selatan (Ruko)
(7, 'exterior', 'Lantai', '3', true, NOW()),
(7, 'exterior', 'Parkir', '2 Mobil', false, NOW()),
(7, 'utilities', 'Listrik', '3500 Watt', false, NOW()),
(7, 'security', 'Satpam', 'Ada', false, NOW()),
(7, 'amenities', 'Lift Barang', 'Ada', false, NOW()),

-- Property 8 - Bekasi
(8, 'interior', 'Kamar Tidur', '3', true, NOW()),
(8, 'interior', 'Kamar Mandi', '2', true, NOW()),
(8, 'exterior', 'Teras', 'Ada', false, NOW()),
(8, 'security', 'CCTV', '24 Jam', false, NOW()),
(8, 'utilities', 'Listrik', '2200 Watt', false, NOW()),

-- Property 9 - Jakarta Selatan (Apartemen)
(9, 'interior', 'Kamar Tidur', '2', true, NOW()),
(9, 'interior', 'Kamar Mandi', '2', true, NOW()),
(9, 'amenities', 'Gym', 'Ada', false, NOW()),
(9, 'utilities', 'AC', '3 Unit', false, NOW()),
(9, 'security', 'Resepsionis 24 Jam', 'Ada', false, NOW()),

-- Property 10 - Jakarta Barat (Ruko)
(10, 'exterior', 'Lantai', '3', true, NOW()),
(10, 'exterior', 'Parkir', '2 Mobil', false, NOW()),
(10, 'utilities', 'Listrik', '3500 Watt', false, NOW()),
(10, 'security', 'Satpam', 'Ada', false, NOW()),
(10, 'utilities', 'Air', 'PDAM', false, NOW());

-- End of V17__202522101006_DATA_SEEDS_PROPERTIES.sql

-- Description: Seed data for property_locations table
INSERT INTO property_locations (
    property_id, poi_type, poi_name, distance_km, travel_time_minutes, created_at
) VALUES
-- Property 1 - Bogor
(1, 'school', 'SMA Negeri 1 Bogor', 1.2, 5, NOW()),
(1, 'mall', 'Botani Square', 2.8, 9, NOW()),
(1, 'hospital', 'RSUD Kota Bogor', 2.1, 7, NOW()),
(1, 'mosque', 'Masjid Raya Bogor', 1.0, 4, NOW()),

-- Property 2 - Jakarta Pusat
(2, 'mall', 'Plaza Indonesia', 0.7, 3, NOW()),
(2, 'transport', 'Stasiun Sudirman', 0.6, 2, NOW()),
(2, 'hospital', 'RS Abdi Waluyo', 1.4, 5, NOW()),
(2, 'bank', 'BNI KC Thamrin', 0.5, 2, NOW()),

-- Property 3 - Bandung
(3, 'school', 'SMA Negeri 3 Bandung', 1.5, 5, NOW()),
(3, 'mall', 'Trans Studio Mall Bandung', 2.2, 7, NOW()),
(3, 'church', 'GKI Maulana Yusuf', 1.1, 4, NOW()),
(3, 'hospital', 'RS Borromeus', 1.8, 6, NOW()),

-- Property 4 - Depok (Cinere)
(4, 'school', 'SMA Negeri 2 Depok', 1.3, 4, NOW()),
(4, 'mall', 'Cinere Bellevue Mall', 2.1, 7, NOW()),
(4, 'hospital', 'RS Mitra Keluarga Depok', 2.5, 8, NOW()),
(4, 'bank', 'BNI Cinere', 1.0, 3, NOW()),

-- Property 5 - Depok (Margonda)
(5, 'mall', 'Margo City', 1.0, 4, NOW()),
(5, 'transport', 'Stasiun Pondok Cina', 0.8, 3, NOW()),
(5, 'hospital', 'RS Bunda Margonda', 1.2, 5, NOW()),
(5, 'office', 'Kantor BNI Margonda', 1.1, 4, NOW()),

-- Property 6 - Bogor (Villa)
(6, 'park', 'Taman Safari Indonesia', 4.5, 12, NOW()),
(6, 'school', 'SD Puncak Lestari', 1.4, 4, NOW()),
(6, 'mosque', 'Masjid Al-Mukhlisin', 0.8, 3, NOW()),
(6, 'hospital', 'RS Ciawi', 3.0, 9, NOW()),

-- Property 7 - Tangerang Selatan (BSD)
(7, 'mall', 'AEON Mall BSD City', 3.5, 10, NOW()),
(7, 'school', 'SMA Al-Azhar BSD', 2.3, 7, NOW()),
(7, 'transport', 'Tol Serpong', 1.8, 5, NOW()),
(7, 'bank', 'BNI BSD Junction', 1.0, 3, NOW()),

-- Property 8 - Bekasi
(8, 'school', 'SMPN 2 Bekasi', 1.1, 4, NOW()),
(8, 'mall', 'Grand Metropolitan Bekasi', 2.9, 9, NOW()),
(8, 'hospital', 'RS Hermina Bekasi', 2.0, 6, NOW()),
(8, 'mosque', 'Masjid Agung Al-Barkah', 1.2, 4, NOW()),

-- Property 9 - Jakarta Selatan (Apartemen)
(9, 'mall', 'Kota Kasablanka', 1.2, 4, NOW()),
(9, 'transport', 'Stasiun Tebet', 1.8, 6, NOW()),
(9, 'hospital', 'RS MMC Kuningan', 1.5, 5, NOW()),
(9, 'office', 'BNI Tower Kuningan', 1.1, 4, NOW()),

-- Property 10 - Jakarta Barat (Ruko)
(10, 'mall', 'Mall Taman Anggrek', 2.2, 7, NOW()),
(10, 'transport', 'Tol Tomang', 1.5, 5, NOW()),
(10, 'hospital', 'RS Royal Taruma', 1.7, 6, NOW()),
(10, 'bank', 'BNI Daan Mogot', 0.9, 3, NOW());
-- End of V17__202522101006_DATA_SEEDS_PROPERTIES.sql


-- Description: Seed data for properties table
INSERT INTO public.properties (
    property_code, developer_id, property_type, listing_type, title, description,
    address, city, province, postal_code, district, village, latitude, longitude,
    land_area, building_area, bedrooms, bathrooms, floors, garage, year_built,
    price, price_per_sqm, maintenance_fee, certificate_type, certificate_number,
    certificate_area, pbb_value, status, availability_date, handover_date,
    is_featured, is_kpr_eligible, min_down_payment_percent, max_loan_term_years,
    slug, meta_title, meta_description, keywords, view_count, inquiry_count,
    favorite_count, created_at, updated_at, published_at
) VALUES
-- Property 1 - PT Bumi Asri Sejahtera (Bogor)
('PROP-2025-0001', 1, 'rumah', 'primary',
 'Cluster Hijau Bumi Asri Bogor',
 'Rumah dua lantai dengan konsep hijau dan ramah lingkungan di kawasan Bogor.',
 'Jl. Raya Bogor No.45', 'Bogor', 'Jawa Barat', '16115', 'Bogor Timur', 'Sindang Barang',
 -6.5899, 106.7911, 150.00, 120.00, 3, 2, 2, 1, 2018,
 850000000.00, 7083333.00, 250000.00, 'shm', 'SHM-BA-001', 150.00, 2500000.00,
 'available', '2025-09-01', '2026-01-01', true, true, 15.00, 20,
 'cluster-hijau-bumi-asri-bogor', 'Cluster Hijau Bumi Asri Bogor',
 'Hunian asri di tengah kota Bogor dengan akses ke tol dan sekolah.',
 'rumah, bogor, hijau, asri, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 2 - PT Maju Sentosa Properti (Jakarta Pusat)
('PROP-2025-0002', 2, 'apartemen', 'primary',
 'Apartemen Sentosa Residence Sudirman',
 'Apartemen premium dengan fasilitas lengkap di jantung kota Jakarta.',
 'Jl. Jendral Sudirman No.88', 'Jakarta Pusat', 'DKI Jakarta', '10220',
 'Tanah Abang', 'Karet Tengsin',
 -6.2057, 106.8224, 90.00, 85.00, 2, 1, 1, 0, 2022,
 2200000000.00, 25882352.00, 350000.00, 'strata', 'STR-MSP-002', 90.00, 5000000.00,
 'available', '2025-09-10', '2026-02-01', true, true, 10.00, 25,
 'apartemen-sentosa-residence-sudirman', 'Apartemen Sentosa Residence Sudirman',
 'Hunian vertikal modern di CBD Sudirman dengan view kota.',
 'apartemen, jakarta, premium, cbd, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 3 - PT Cipta Karya Nusantara (Bandung)
('PROP-2025-0003', 3, 'rumah', 'primary',
 'Cipta Karya Residence Bandung',
 'Cluster eksklusif dengan desain minimalis dan lingkungan tenang di Bandung.',
 'Jl. Soekarno Hatta No.12', 'Bandung', 'Jawa Barat', '40286',
 'Batununggal', 'Kujangsari',
 -6.9519, 107.6425, 180.00, 140.00, 3, 2, 2, 1, 2021,
 1250000000.00, 8928571.00, 300000.00, 'shm', 'SHM-CKN-003', 180.00, 3200000.00,
 'available', '2025-08-25', '2026-02-15', false, true, 20.00, 20,
 'cipta-karya-residence-bandung', 'Cipta Karya Residence Bandung',
 'Rumah minimalis di kawasan berkembang Bandung Selatan.',
 'rumah, bandung, cluster, minimalis, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 4 - PT Griya Mandiri Abadi (Depok)
('PROP-2025-0004', 4, 'rumah', 'primary',
 'Griya Mandiri Cinere Residence',
 'Rumah dua lantai dengan desain tropis modern dan area hijau luas.',
 'Jl. Raya Cinere No.25', 'Depok', 'Jawa Barat', '16514',
 'Cinere', 'Pangkalan Jati',
 -6.3524, 106.7883, 160.00, 130.00, 3, 3, 2, 1, 2023,
 1450000000.00, 11153846.00, 280000.00, 'shm', 'SHM-GMA-004', 160.00, 2800000.00,
 'available', '2025-09-10', '2026-02-28', false, true, 15.00, 20,
 'griya-mandiri-cinere-residence', 'Griya Mandiri Cinere Residence',
 'Rumah keluarga di Cinere dengan akses cepat ke tol Desari.',
 'rumah, depok, cinere, modern, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 5 - PT Citra Alam Sejahtera (Depok)
('PROP-2025-0005', 5, 'rumah', 'primary',
 'Citra Alam Margonda Estate',
 'Hunian hijau dengan lingkungan tenang dan aman di Margonda.',
 'Jl. Margonda Raya No.120', 'Depok', 'Jawa Barat', '16424',
 'Beji', 'Kemiri Muka',
 -6.3851, 106.8277, 120.00, 100.00, 2, 2, 1, 1, 2020,
 980000000.00, 9800000.00, 250000.00, 'shm', 'SHM-CAS-005', 120.00, 2500000.00,
 'available', '2025-09-15', '2026-01-15', false, true, 10.00, 20,
 'citra-alam-margonda-estate', 'Citra Alam Margonda Estate',
 'Perumahan hijau dengan sistem keamanan 24 jam.',
 'rumah, depok, margonda, hijau, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 6 - PT Puncak Indah Lestari (Bogor)
('PROP-2025-0006', 6, 'villa', 'primary',
 'Villa Puncak Indah Lestari',
 'Villa mewah di kawasan Puncak dengan pemandangan pegunungan.',
 'Jl. Raya Puncak KM 80', 'Bogor', 'Jawa Barat', '16770',
 'Cisarua', 'Tugu Utara',
 -6.7132, 106.9531, 500.00, 300.00, 4, 3, 2, 1, 2019,
 3500000000.00, 11666666.00, 500000.00, 'shm', 'SHM-PIL-006', 500.00, 7000000.00,
 'available', '2025-08-30', '2026-03-01', true, false, 25.00, 15,
 'villa-puncak-indah-lestari', 'Villa Puncak Indah Lestari Bogor',
 'Villa pribadi dengan kolam renang dan view gunung.',
 'villa, puncak, bogor, resort, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 7 - PT BSD Central Property (Tangerang)
('PROP-2025-0007', 7, 'ruko', 'primary',
 'BSD Central Business Ruko',
 'Ruko 3 lantai di kawasan komersial BSD City.',
 'Jl. BSD Boulevard No.5', 'Tangerang Selatan', 'Banten', '15345',
 'Serpong', 'Lengkong Wetan',
 -6.2973, 106.6808, 120.00, 240.00, 0, 1, 3, 0, 2018,
 2500000000.00, 10416666.00, 400000.00, 'shgb', 'HGB-BCD-007', 120.00, 3500000.00,
 'available', '2025-09-05', '2026-02-10', true, true, 20.00, 20,
 'bsd-central-business-ruko', 'BSD Central Business Ruko',
 'Ruko strategis untuk bisnis di kawasan BSD City.',
 'ruko, bsd, tangerang, komersial, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 8 - PT Harmony Estate (Bekasi)
('PROP-2025-0008', 8, 'rumah', 'primary',
 'Harmony Estate Bekasi Tropis',
 'Rumah keluarga dengan konsep tropis modern dan taman hijau.',
 'Jl. Boulevard Barat No.88', 'Bekasi', 'Jawa Barat', '17116',
 'Bekasi Barat', 'Jakasampurna',
 -6.2201, 106.9805, 135.00, 115.00, 3, 2, 2, 1, 2022,
 1250000000.00, 10869565.00, 280000.00, 'shm', 'SHM-HEB-008', 135.00, 3000000.00,
 'available', '2025-10-01', '2026-03-10', false, true, 15.00, 20,
 'harmony-estate-bekasi-tropis', 'Harmony Estate Bekasi Tropis',
 'Hunian tropis dengan konsep ramah lingkungan di Bekasi.',
 'rumah, bekasi, tropis, modern, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 9 - PT Nusantara Properti Prima (Jakarta Selatan)
('PROP-2025-0009', 9, 'apartemen', 'primary',
 'Nusantara Tower Kuningan',
 'Apartemen mewah dengan fasilitas premium di kawasan Kuningan Jakarta Selatan.',
 'Jl. Gatot Subroto No.20', 'Jakarta Selatan', 'DKI Jakarta', '12210',
 'Setiabudi', 'Kuningan Barat',
 -6.2295, 106.8272, 95.00, 85.00, 2, 2, 1, 0, 2023,
 2700000000.00, 31764705.00, 400000.00, 'strata', 'STR-NPP-009', 95.00, 5000000.00,
 'available', '2025-09-20', '2026-03-01', true, true, 10.00, 25,
 'nusantara-tower-kuningan', 'Nusantara Tower Kuningan',
 'Apartemen premium dengan lokasi strategis dekat perkantoran utama.',
 'apartemen, kuningan, jakarta selatan, mewah, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Property 10 - PT Mega Graha Sentosa (Jakarta Barat)
('PROP-2025-0010', 10, 'ruko', 'primary',
 'Mega Graha Business Park',
 'Ruko modern di pusat bisnis Jakarta Barat, cocok untuk usaha dan investasi.',
 'Jl. Daan Mogot No.55', 'Jakarta Barat', 'DKI Jakarta', '11460',
 'Grogol Petamburan', 'Tanjung Duren Utara',
 -6.1639, 106.7862, 140.00, 250.00, 0, 1, 3, 0, 2020,
 2800000000.00, 11200000.00, 420000.00, 'shgb', 'HGB-MGS-010', 140.00, 4000000.00,
 'available', '2025-09-25', '2026-02-15', true, true, 20.00, 20,
 'mega-graha-business-park', 'Mega Graha Business Park Jakarta Barat',
 'Ruko strategis di Daan Mogot dengan area parkir luas.',
 'ruko, jakarta barat, bisnis, komersial, bni griya',
 0,0,0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
-- End of V17__202522101006_DATA_SEEDS_PROPERTIES.sql