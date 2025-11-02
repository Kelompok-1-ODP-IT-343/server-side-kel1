-- Change partnership_level enum values and migrate data
-- Old values: 'BRONZE', 'SILVER', 'GOLD', 'PLATINUM'
-- New values: 'TOP_SELECTED_DEVELOPER', 'DEVELOPER_KERJA_SAMA'

-- 1) Create the new enum type with desired values
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type t WHERE t.typname = 'partnership_level_new') THEN
    CREATE TYPE partnership_level_new AS ENUM ('TOP_SELECTED_DEVELOPER', 'DEVELOPER_KERJA_SAMA');
  END IF;
END$$;

-- 2) Alter column to use the new enum, migrating existing rows
ALTER TABLE developers
  ALTER COLUMN partnership_level TYPE partnership_level_new
  USING (
    CASE partnership_level::text
      WHEN 'PLATINUM' THEN 'TOP_SELECTED_DEVELOPER'::partnership_level_new
      WHEN 'GOLD' THEN 'TOP_SELECTED_DEVELOPER'::partnership_level_new
      WHEN 'SILVER' THEN 'DEVELOPER_KERJA_SAMA'::partnership_level_new
      WHEN 'BRONZE' THEN 'DEVELOPER_KERJA_SAMA'::partnership_level_new
      WHEN 'TOP_SELECTED_DEVELOPER' THEN 'TOP_SELECTED_DEVELOPER'::partnership_level_new
      WHEN 'DEVELOPER_KERJA_SAMA' THEN 'DEVELOPER_KERJA_SAMA'::partnership_level_new
      ELSE NULL
    END
  );

-- 3) Drop the old enum type and rename the new one to keep the original type name
DO $$
BEGIN
  -- Drop the old enum type if it exists and is unused
  IF EXISTS (SELECT 1 FROM pg_type t WHERE t.typname = 'partnership_level') THEN
    -- Ensure no columns still use the old type
    IF NOT EXISTS (
      SELECT 1
      FROM pg_attribute a
      JOIN pg_class c ON a.attrelid = c.oid
      JOIN pg_type  ty ON a.atttypid = ty.oid
      WHERE ty.typname = 'partnership_level'
        AND a.attnum > 0
        AND NOT a.attisdropped
    ) THEN
      DROP TYPE partnership_level;
    END IF;
  END IF;

  -- Rename new type to the canonical name if not already
  IF EXISTS (SELECT 1 FROM pg_type t WHERE t.typname = 'partnership_level_new')
     AND NOT EXISTS (SELECT 1 FROM pg_type t WHERE t.typname = 'partnership_level') THEN
    ALTER TYPE partnership_level_new RENAME TO partnership_level;
  END IF;
END$$;
