ALTER TABLE developers
ALTER COLUMN partnership_level TYPE varchar USING partnership_level::text;

ALTER TABLE developers
ALTER COLUMN specialization TYPE varchar USING specialization::text;

ALTER TABLE developers
ALTER COLUMN status TYPE varchar USING status::text;