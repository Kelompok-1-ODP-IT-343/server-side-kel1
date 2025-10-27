INSERT INTO property_images (
    property_id, image_type, image_category, file_name, file_path, file_size,
    mime_type, width, height, alt_text, caption, sort_order, is_primary, uploaded_at
) VALUES
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0001'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/01.jpg', 512000, 'image/jpeg', 1920, 1080, 'Tampak depan rumah Bogor', 'Rumah cluster hijau di Bogor', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0002'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/03.jpg', 480000, 'image/jpeg', 1920, 1080, 'Apartemen Jakarta tampak depan', 'Gedung apartemen modern di Sudirman', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0003'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/05.jpg', 490000, 'image/jpeg', 1920, 1080, 'Perumahan Bandung tampak depan', 'Cluster asri di Bandung Timur', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0004'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/07.jpg', 510000, 'image/jpeg', 1920, 1080, 'Tampak depan rumah Depok', 'Rumah 2 lantai di Cinere', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0005'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/09.jpg', 495000, 'image/jpeg', 1920, 1080, 'Perumahan hijau Depok', 'Hunian ramah lingkungan di Margonda', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0006'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/11.jpg', 530000, 'image/jpeg', 1920, 1080, 'Villa Puncak tampak depan', 'Villa mewah dengan pemandangan gunung', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0007'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/13.jpg', 500000, 'image/jpeg', 1920, 1080, 'Ruko BSD tampak depan', 'Ruko 3 lantai area komersial BSD', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0008'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/15.jpg', 480000, 'image/jpeg', 1920, 1080, 'Townhouse Bekasi tampak depan', 'Townhouse modern dengan carport', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0009'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/17.jpg', 510000, 'image/jpeg', 1920, 1080, 'Apartemen Gatot Subroto tampak depan', 'Gedung apartemen mewah di Jakarta', 1, true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0010'), 'EXTERIOR', 'MAIN', gen_random_uuid()::text, 'https://is3.cloudhost.id/griyabni01/Sample01/19.jpg', 550000, 'image/jpeg', 1920, 1080, 'Ruko Daan Mogot tampak depan', 'Ruko komersial strategis di Jakarta Barat', 1, true, NOW())

    ON CONFLICT (file_name) DO NOTHING;


-- insert features

INSERT INTO property_features (
    property_id, feature_category, feature_name, feature_value, is_highlight, created_at
) VALUES
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0001'), 'INTERIOR', 'Kamar Tidur', '3', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0001'), 'EXTERIOR', 'Garasi', '1 Mobil', false, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0001'), 'UTILITIES', 'Listrik', '2200 Watt', false, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0002'), 'INTERIOR', 'Kamar Tidur', '2', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0002'), 'AMENITIES', 'Kolam Renang', 'Umum', false, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0003'), 'INTERIOR', 'Kamar Tidur', '3', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0003'), 'EXTERIOR', 'Carport', '1 Mobil', false, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0004'), 'INTERIOR', 'Kamar Tidur', '3', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0004'), 'EXTERIOR', 'Garasi', '1 Mobil', false, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0005'), 'INTERIOR', 'Kamar Tidur', '2', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0005'), 'AMENITIES', 'Taman', 'Ada', false, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0006'), 'INTERIOR', 'Kamar Tidur', '4', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0006'), 'AMENITIES', 'Kolam Renang', 'Private', true, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0007'), 'EXTERIOR', 'Lantai', '3', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0007'), 'AMENITIES', 'Lift Barang', 'Ada', false, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0008'), 'INTERIOR', 'Kamar Tidur', '3', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0008'), 'EXTERIOR', 'Teras', 'Ada', false, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0009'), 'INTERIOR', 'Kamar Tidur', '2', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0009'), 'AMENITIES', 'Gym', 'Ada', false, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0010'), 'EXTERIOR', 'Lantai', '3', true, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0010'), 'UTILITIES', 'Air', 'PDAM', false, NOW());


--insert locations

INSERT INTO property_locations (
    property_id, poi_type, poi_name, distance_km, travel_time_minutes, created_at
) VALUES
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0001'), 'SCHOOL', 'SMA Negeri 1 Bogor', 1.2, 5, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0001'), 'MALL', 'Botani Square', 2.8, 9, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0001'), 'HOSPITAL', 'RSUD Kota Bogor', 2.1, 7, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0002'), 'MALL', 'Plaza Indonesia', 0.7, 3, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0002'), 'HOSPITAL', 'RS Abdi Waluyo', 1.4, 5, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0003'), 'SCHOOL', 'SMA Negeri 3 Bandung', 1.5, 5, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0003'), 'MALL', 'Trans Studio Mall Bandung', 2.2, 7, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0004'), 'SCHOOL', 'SMA Negeri 2 Depok', 1.3, 4, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0004'), 'MALL', 'Cinere Bellevue Mall', 2.1, 7, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0005'), 'MALL', 'Margo City', 1.0, 4, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0005'), 'TRANSPORT', 'Stasiun Pondok Cina', 0.8, 3, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0006'), 'PARK', 'Taman Safari Indonesia', 4.5, 12, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0006'), 'MOSQUE', 'Masjid Al-Mukhlisin', 0.8, 3, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0007'), 'MALL', 'AEON Mall BSD City', 3.5, 10, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0007'), 'BANK', 'BNI BSD Junction', 1.0, 3, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0008'), 'SCHOOL', 'SMPN 2 Bekasi', 1.1, 4, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0008'), 'MALL', 'Grand Metropolitan Bekasi', 2.9, 9, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0009'), 'MALL', 'Kota Kasablanka', 1.2, 4, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0009'), 'HOSPITAL', 'RS MMC Kuningan', 1.5, 5, NOW()),

      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0010'), 'MALL', 'Mall Taman Anggrek', 2.2, 7, NOW()),
      ((SELECT id FROM properties WHERE property_code = 'PROP-2025-0010'), 'BANK', 'BNI Daan Mogot', 0.9, 3, NOW());
