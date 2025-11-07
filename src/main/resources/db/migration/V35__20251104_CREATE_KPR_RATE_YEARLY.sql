CREATE TABLE IF NOT EXISTS kpr_rate_yearly (
    id SERIAL PRIMARY KEY,
    kpr_rate_id INTEGER NOT NULL REFERENCES kpr_rates(id) ON DELETE CASCADE,
    tenor INTEGER NOT NULL CHECK (tenor BETWEEN 1 AND 30),
    year INTEGER NOT NULL CHECK (year >= 1 AND year <= tenor),
    rate DECIMAL(5,4) NOT NULL,

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Optional index untuk mempercepat pencarian rate per tenor dan tahun
CREATE INDEX IF NOT EXISTS idx_kpr_rate_yearly_rate_id ON kpr_rate_yearly (kpr_rate_id);
CREATE INDEX IF NOT EXISTS idx_kpr_rate_yearly_tenor_year ON kpr_rate_yearly (tenor, year);
