-- V23__make_nullable_user_profiles.sql

ALTER TABLE user_profiles
    ALTER COLUMN gender DROP NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN marital_status DROP NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN address DROP NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN city DROP NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN province DROP NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN postal_code DROP NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN nik DROP NOT NULL;

ALTER TABLE users
    ALTER COLUMN phone DROP NOT NULL;



