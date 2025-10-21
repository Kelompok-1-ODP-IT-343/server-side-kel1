-- Migration to remove created_by column from kpr_rates table
-- This column is not needed for the application functionality

-- Drop the created_by column from kpr_rates table
ALTER TABLE kpr_rates DROP COLUMN IF EXISTS created_by;
