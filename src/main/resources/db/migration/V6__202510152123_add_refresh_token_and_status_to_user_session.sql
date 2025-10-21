-- Add columns for refresh_token and status
ALTER TABLE user_sessions
    ADD COLUMN refresh_token VARCHAR(512);

ALTER TABLE user_sessions
    ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL;

ALTER TABLE user_sessions ADD CONSTRAINT uq_user_sessions_refresh_token UNIQUE (refresh_token);

-- Create indexes to improve lookup performance
CREATE INDEX idx_user_sessions_refresh_token ON user_sessions (refresh_token);
CREATE INDEX idx_user_sessions_status ON user_sessions (status);
