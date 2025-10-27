-- 1️⃣ Lepas foreign key yang masih nempel
ALTER TABLE kpr_applications
DROP CONSTRAINT IF EXISTS kpr_applications_current_approval_level_fkey;

-- 2️⃣ Hapus kolom yang gak kepake
ALTER TABLE kpr_applications
DROP COLUMN IF EXISTS current_approval_level;

-- 3️⃣ (Opsional) Hapus tabel approval_levels kalau memang gak dipakai lagi
DROP TABLE IF EXISTS approval_levels CASCADE;
