-- Align dedupe_groups table with DedupeGroup entity definition

-- Add entity_type column (required in entity)
ALTER TABLE dedupe_groups
    ADD COLUMN IF NOT EXISTS entity_type VARCHAR(50);

-- Add status column (required in entity)
ALTER TABLE dedupe_groups
    ADD COLUMN IF NOT EXISTS status VARCHAR(30);

-- Add created_by column as a reference to users table
ALTER TABLE dedupe_groups
    ADD COLUMN IF NOT EXISTS created_by UUID REFERENCES users(id);

-- Add note column
ALTER TABLE dedupe_groups
    ADD COLUMN IF NOT EXISTS note VARCHAR(1000);

-- Ensure created_at column exists (already in V1, but keep for safety)
ALTER TABLE dedupe_groups
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT now();

-- Initialize required fields for existing rows
UPDATE dedupe_groups
SET
    entity_type = COALESCE(entity_type, 'NEEDS_REQUEST'),
    status = COALESCE(status, 'OPEN')
WHERE entity_type IS NULL OR status IS NULL;


