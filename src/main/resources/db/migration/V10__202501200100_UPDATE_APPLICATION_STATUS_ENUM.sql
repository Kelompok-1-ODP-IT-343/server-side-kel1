-- Migration to add new enum values to application_status
-- This fixes the JDBC exception where enum values don't match between database and Java code
-- Part 1: Add new enum values only (they must be committed before use)

-- Add missing enum values to application_status
ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'DOCUMENT_VERIFICATION';
ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'PROPERTY_APPRAISAL';
ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'CREDIT_ANALYSIS';
ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'APPROVAL_PENDING';
ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'DISBURSED';

-- Note: PostgreSQL doesn't allow removing enum values directly
-- The old values ('DRAFT', 'UNDER_REVIEW') will remain but won't be used in Java code
-- This is safe as long as no existing data uses these values

-- Data migration will be handled in the next migration file (V11)
-- to ensure enum values are committed before use
