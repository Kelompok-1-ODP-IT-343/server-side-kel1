-- Migration: Add ltv_ratio column to kpr_applications table
-- Date: 2025-01-20 01:03
-- Description: Add missing ltv_ratio column that exists in KprApplication entity but not in database schema

ALTER TABLE kpr_applications
ADD COLUMN ltv_ratio decimal(5,4) NOT NULL DEFAULT 0.0000;

-- Add comment for documentation
COMMENT ON COLUMN kpr_applications.ltv_ratio IS 'Loan to Value ratio calculated as (loan_amount / property_value)';
