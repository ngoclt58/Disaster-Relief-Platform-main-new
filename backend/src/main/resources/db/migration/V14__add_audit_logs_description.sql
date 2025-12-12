-- Add missing columns to audit_logs table to match AuditLog entity

-- Add description column
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS description TEXT;

-- Add user_id column (String type as per entity)
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS user_id VARCHAR(255);

-- Add user_role column
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS user_role VARCHAR(50);

-- Add target_user_id column
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS target_user_id VARCHAR(255);

-- Add timestamp column (entity uses @CreatedDate with name "timestamp")
-- Note: created_at already exists, but entity expects "timestamp"
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS timestamp TIMESTAMPTZ;

-- If timestamp is null, copy from created_at
UPDATE audit_logs SET timestamp = created_at WHERE timestamp IS NULL;

-- Set default for timestamp
ALTER TABLE audit_logs ALTER COLUMN timestamp SET DEFAULT CURRENT_TIMESTAMP;

-- Fix column types to match entity
-- Change ip_address from INET to VARCHAR(255) to match String type in entity
ALTER TABLE audit_logs ALTER COLUMN ip_address TYPE VARCHAR(255) USING ip_address::text;

-- Ensure user_agent is TEXT (should already be, but make sure)
ALTER TABLE audit_logs ALTER COLUMN user_agent TYPE TEXT;

