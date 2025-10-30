ALTER TABLE user_sessions
    DROP CONSTRAINT IF EXISTS fk_user_sessions_user,
    ADD  CONSTRAINT fk_user_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_profiles
    DROP CONSTRAINT user_profiles_user_id_fkey,
    ADD CONSTRAINT user_profiles_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;