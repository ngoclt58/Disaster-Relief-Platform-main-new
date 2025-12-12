-- Align dedupe_links table with DedupeLink entity definition

-- Add entity_id column (required by entity)
ALTER TABLE dedupe_links
    ADD COLUMN IF NOT EXISTS entity_id UUID;

-- Add score column
ALTER TABLE dedupe_links
    ADD COLUMN IF NOT EXISTS score DOUBLE PRECISION;

-- Add reason column
ALTER TABLE dedupe_links
    ADD COLUMN IF NOT EXISTS reason VARCHAR(1000);

-- For backward compatibility: if request_id exists and entity_id is null,
-- copy request_id into entity_id (assuming old schema used request_id)
UPDATE dedupe_links
SET entity_id = request_id
WHERE entity_id IS NULL AND request_id IS NOT NULL;


