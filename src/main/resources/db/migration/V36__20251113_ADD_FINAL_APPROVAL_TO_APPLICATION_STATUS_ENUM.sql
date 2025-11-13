-- Add FINAL_APPROVAL to application_status enum for KPR application statuses
-- Context: Align application-level status with workflow stage 'FINAL_APPROVAL'
-- Note: PostgreSQL 12+ supports BEFORE/AFTER positioning; if unsupported, the
--       value will be appended at the end. Using IF NOT EXISTS for idempotency.

DO $$
BEGIN
  BEGIN
    ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'FINAL_APPROVAL' BEFORE 'APPROVED';
  EXCEPTION
    WHEN invalid_parameter_value THEN
      -- Fallback for environments that don't support BEFORE/AFTER
      ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'FINAL_APPROVAL';
  END;
END $$;

-- No data backfill performed here; business logic will set status transitions
-- to FINAL_APPROVAL as part of workflow progression.