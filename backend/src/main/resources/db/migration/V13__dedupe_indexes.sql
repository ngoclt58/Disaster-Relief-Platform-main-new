-- Dedupe indexes (moved from V3 to resolve duplicate version)

-- Ensure dedupe groups indexes exist
-- Note: dedupe_groups table only has: id, reason, created_at
CREATE INDEX IF NOT EXISTS idx_dedupe_groups_created_at ON dedupe_groups(created_at);

-- Ensure dedupe links indexes exist
CREATE INDEX IF NOT EXISTS idx_dedupe_links_group ON dedupe_links(group_id);
CREATE INDEX IF NOT EXISTS idx_dedupe_links_request ON dedupe_links(request_id);
CREATE INDEX IF NOT EXISTS idx_dedupe_links_created_at ON dedupe_links(created_at);

