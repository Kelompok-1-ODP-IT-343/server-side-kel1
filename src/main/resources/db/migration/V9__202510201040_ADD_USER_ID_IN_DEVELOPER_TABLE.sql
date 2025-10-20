ALTER TABLE developers
    ADD COLUMN user_id BIGINT NOT NULL UNIQUE,
    ADD CONSTRAINT fk_developer_user FOREIGN KEY (user_id) REFERENCES users(id);