-- Migration to update existing application_status data
-- Part 2: Update existing records to use new enum values
-- This runs after V10 to ensure new enum values are committed

-- Update any existing records that might use the old enum values
-- Convert 'UNDER_REVIEW' to 'DOCUMENT_VERIFICATION' if any exist
UPDATE kpr_applications
SET status = 'DOCUMENT_VERIFICATION'
WHERE status = 'UNDER_REVIEW';

-- Convert 'DRAFT' to 'SUBMITTED' if any exist
UPDATE kpr_applications
SET status = 'SUBMITTED'
WHERE status = 'DRAFT';

-- Log the migration results
-- Note: These comments serve as documentation for the migration
