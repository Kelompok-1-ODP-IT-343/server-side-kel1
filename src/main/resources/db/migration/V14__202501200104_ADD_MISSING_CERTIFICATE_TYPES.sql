-- Migration: Add missing certificate types to property_certificate_type enum
-- Date: 2025-01-20 01:04
-- Description: Add GIRIK and PETOK_D values that exist in Property.CertificateType enum but missing in database

-- Add missing enum values to property_certificate_type
ALTER TYPE property_certificate_type ADD VALUE IF NOT EXISTS 'GIRIK';
ALTER TYPE property_certificate_type ADD VALUE IF NOT EXISTS 'PETOK_D';

-- Note: PostgreSQL doesn't allow removing enum values directly
-- All existing values (SHM, HGB, HGU, HP) will remain available
-- This ensures compatibility between Java enum and database enum