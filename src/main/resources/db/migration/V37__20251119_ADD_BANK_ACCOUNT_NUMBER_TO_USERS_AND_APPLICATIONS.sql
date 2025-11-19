ALTER TABLE users ADD COLUMN IF NOT EXISTS bank_account_number VARCHAR(30);
ALTER TABLE kpr_applications ADD COLUMN IF NOT EXISTS bank_account_number VARCHAR(30);
