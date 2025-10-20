-- Migration: Add OTHER to document_type enum
-- Date: 2025-10-20
-- Description: Menambahkan nilai enum 'OTHER' ke tipe PostgreSQL document_type agar sesuai dengan enum Java

ALTER TYPE document_type ADD VALUE IF NOT EXISTS 'OTHER';

-- Catatan:
-- - PostgreSQL menambahkan nilai enum di akhir urutan.
-- - IF NOT EXISTS mencegah error jika nilai sudah ada di environment tertentu.
