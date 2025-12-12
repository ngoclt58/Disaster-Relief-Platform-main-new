-- Add indexes for media table for better query performance
CREATE INDEX IF NOT EXISTS idx_media_owner_user_id ON media(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_media_type ON media(type);
CREATE INDEX IF NOT EXISTS idx_media_created_at ON media(created_at);
CREATE INDEX IF NOT EXISTS idx_media_taken_at ON media(taken_at);
CREATE INDEX IF NOT EXISTS idx_media_redacted ON media(redacted);

-- Add spatial index for media location queries
CREATE INDEX IF NOT EXISTS idx_media_geom_point ON media USING GIST(geom_point);

-- Add comments
COMMENT ON INDEX idx_media_geom_point IS 'Spatial index for location-based media queries';
COMMENT ON INDEX idx_media_owner_user_id IS 'Index for querying media by owner';
COMMENT ON INDEX idx_media_type IS 'Index for filtering media by type (image, video, etc.)';
