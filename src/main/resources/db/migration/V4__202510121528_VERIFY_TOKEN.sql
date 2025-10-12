-- Tabel token verifikasi email
CREATE TABLE verification_token (
  id           BIGSERIAL PRIMARY KEY,
  user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token        VARCHAR(128) NOT NULL UNIQUE,
  expires_at   TIMESTAMPTZ NOT NULL,
  used_at      TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX verification_token_user_idx ON verification_token(user_id);
CREATE INDEX verification_token_expires_idx ON verification_token(expires_at);

-- ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT false;
