-- Add columns for refresh_token and status
ALTER TABLE user_session
    ADD COLUMN refresh_token VARCHAR(512);

ALTER TABLE user_session
    ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL;

ALTER TABLE user_session ADD CONSTRAINT uq_user_session_refresh_token UNIQUE (refresh_token);

-- Create indexes to improve lookup performance
CREATE INDEX idx_user_session_refresh_token ON user_session (refresh_token);
CREATE INDEX idx_user_session_status ON user_session (status);
