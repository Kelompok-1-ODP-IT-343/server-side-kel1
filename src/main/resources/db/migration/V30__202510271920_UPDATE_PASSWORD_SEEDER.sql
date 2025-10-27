-- Update password for all users with role_id 3 (DEVELOPER role)
-- This is for seeder purposes, updating all developer accounts with the same password hash
-- Password hash: $2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG

UPDATE users
SET password_hash = '$2a$12$ro.g3snEyYaadZFqgmuckub6.TGGQ12Nf1zRo3amaBSBpd3997GMG',
    updated_at = NOW()
WHERE role_id = 3;

-- Log the number of affected rows for verification
-- This will help track how many developer accounts were updated
