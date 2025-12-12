-- Create offline map cache tables for areas with poor connectivity
-- This migration creates tables for offline map caching, tiles, and downloads

-- Create offline_map_caches table
CREATE TABLE offline_map_caches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    region_id VARCHAR(255) NOT NULL,
    region_name VARCHAR(255) NOT NULL,
    bounds GEOMETRY(Geometry, 4326) NOT NULL,
    zoom_levels TEXT NOT NULL, -- JSON array of zoom levels
    map_type VARCHAR(50) NOT NULL,
    tile_source VARCHAR(500) NOT NULL,
    tile_format VARCHAR(10) NOT NULL DEFAULT 'png',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    total_tiles BIGINT NOT NULL DEFAULT 0,
    downloaded_tiles BIGINT NOT NULL DEFAULT 0,
    cache_size_bytes BIGINT NOT NULL DEFAULT 0,
    estimated_size_bytes BIGINT,
    download_progress DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    download_started_at TIMESTAMP,
    download_completed_at TIMESTAMP,
    last_accessed_at TIMESTAMP,
    expires_at TIMESTAMP,
    is_compressed BOOLEAN NOT NULL DEFAULT false,
    compression_ratio DOUBLE PRECISION,
    metadata JSONB,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for offline_map_caches
CREATE INDEX idx_offline_cache_name ON offline_map_caches(name);
CREATE INDEX idx_offline_cache_region ON offline_map_caches(region_id);
CREATE INDEX idx_offline_cache_status ON offline_map_caches(status);
CREATE INDEX idx_offline_cache_priority ON offline_map_caches(priority);
CREATE INDEX idx_offline_cache_geom ON offline_map_caches USING GIST(bounds);
CREATE INDEX idx_offline_cache_map_type ON offline_map_caches(map_type);
CREATE INDEX idx_offline_cache_created_by ON offline_map_caches(created_by);
CREATE INDEX idx_offline_cache_expires ON offline_map_caches(expires_at);
CREATE INDEX idx_offline_cache_last_accessed ON offline_map_caches(last_accessed_at);

-- Create offline_map_tiles table
CREATE TABLE offline_map_tiles (
    id BIGSERIAL PRIMARY KEY,
    offline_map_cache_id BIGINT NOT NULL REFERENCES offline_map_caches(id) ON DELETE CASCADE,
    z INTEGER NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    tile_key VARCHAR(255) NOT NULL,
    tile_url VARCHAR(500) NOT NULL,
    file_path VARCHAR(500),
    file_size_bytes BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    download_attempts INTEGER NOT NULL DEFAULT 0,
    last_download_attempt TIMESTAMP,
    last_accessed_at TIMESTAMP,
    checksum VARCHAR(255),
    is_compressed BOOLEAN NOT NULL DEFAULT false,
    compression_ratio DOUBLE PRECISION,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(offline_map_cache_id, tile_key)
);

-- Create indexes for offline_map_tiles
CREATE INDEX idx_offline_tile_cache ON offline_map_tiles(offline_map_cache_id);
CREATE INDEX idx_offline_tile_coords ON offline_map_tiles(z, x, y);
CREATE INDEX idx_offline_tile_status ON offline_map_tiles(status);
CREATE INDEX idx_offline_tile_accessed ON offline_map_tiles(last_accessed_at);
CREATE INDEX idx_offline_tile_attempts ON offline_map_tiles(download_attempts);
CREATE INDEX idx_offline_tile_compressed ON offline_map_tiles(is_compressed);
CREATE INDEX idx_offline_tile_checksum ON offline_map_tiles(checksum);

-- Create offline_map_downloads table
CREATE TABLE offline_map_downloads (
    id BIGSERIAL PRIMARY KEY,
    offline_map_cache_id BIGINT NOT NULL REFERENCES offline_map_caches(id) ON DELETE CASCADE,
    download_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_tiles BIGINT NOT NULL DEFAULT 0,
    downloaded_tiles BIGINT NOT NULL DEFAULT 0,
    failed_tiles BIGINT NOT NULL DEFAULT 0,
    progress_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    download_speed_bytes_per_sec BIGINT,
    estimated_completion_time TIMESTAMP,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    download_config JSONB,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(offline_map_cache_id, download_id)
);

-- Create indexes for offline_map_downloads
CREATE INDEX idx_offline_download_cache ON offline_map_downloads(offline_map_cache_id);
CREATE INDEX idx_offline_download_status ON offline_map_downloads(status);
CREATE INDEX idx_offline_download_created ON offline_map_downloads(created_at);
CREATE INDEX idx_offline_download_started ON offline_map_downloads(started_at);
CREATE INDEX idx_offline_download_completed ON offline_map_downloads(completed_at);

-- Add comments to tables
COMMENT ON TABLE offline_map_caches IS 'Offline map caches for areas with poor connectivity';
COMMENT ON TABLE offline_map_tiles IS 'Individual map tiles within offline caches';
COMMENT ON TABLE offline_map_downloads IS 'Download sessions for offline map caches';

-- Add comments to key columns
COMMENT ON COLUMN offline_map_caches.bounds IS 'Geographic bounds of the cached area';
COMMENT ON COLUMN offline_map_caches.zoom_levels IS 'JSON array of zoom levels to cache';
COMMENT ON COLUMN offline_map_caches.tile_source IS 'URL template for tile source';
COMMENT ON COLUMN offline_map_caches.download_progress IS 'Download progress percentage (0.0 to 1.0)';
COMMENT ON COLUMN offline_map_caches.cache_size_bytes IS 'Size of cached data in bytes';
COMMENT ON COLUMN offline_map_caches.estimated_size_bytes IS 'Estimated total size in bytes';
COMMENT ON COLUMN offline_map_tiles.tile_key IS 'Unique key for the tile (z/x/y)';
COMMENT ON COLUMN offline_map_tiles.tile_url IS 'Original tile URL';
COMMENT ON COLUMN offline_map_tiles.file_path IS 'Local file path for the tile';
COMMENT ON COLUMN offline_map_tiles.checksum IS 'File checksum for integrity verification';
COMMENT ON COLUMN offline_map_downloads.download_id IS 'Unique download session ID';
COMMENT ON COLUMN offline_map_downloads.progress_percentage IS 'Download progress percentage (0.0 to 100.0)';
COMMENT ON COLUMN offline_map_downloads.download_speed_bytes_per_sec IS 'Current download speed in bytes per second';

-- Create function to update cache progress
CREATE OR REPLACE FUNCTION update_cache_progress()
RETURNS TRIGGER AS $$
BEGIN
    -- Update cache progress when tile status changes
    IF TG_OP = 'UPDATE' AND OLD.status != NEW.status THEN
        UPDATE offline_map_caches 
        SET 
            downloaded_tiles = (
                SELECT COUNT(*) 
                FROM offline_map_tiles 
                WHERE offline_map_cache_id = NEW.offline_map_cache_id 
                AND status = 'COMPLETED'
            ),
            download_progress = (
                SELECT CASE 
                    WHEN total_tiles > 0 THEN 
                        CAST(COUNT(*) AS DOUBLE PRECISION) / total_tiles 
                    ELSE 0.0 
                END
                FROM offline_map_tiles 
                WHERE offline_map_cache_id = NEW.offline_map_cache_id 
                AND status = 'COMPLETED'
            ),
            cache_size_bytes = (
                SELECT COALESCE(SUM(file_size_bytes), 0)
                FROM offline_map_tiles 
                WHERE offline_map_cache_id = NEW.offline_map_cache_id 
                AND status = 'COMPLETED'
            )
        WHERE id = NEW.offline_map_cache_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to update cache progress
CREATE TRIGGER trigger_update_cache_progress
    AFTER UPDATE ON offline_map_tiles
    FOR EACH ROW
    EXECUTE FUNCTION update_cache_progress();

-- Create function to clean up old tiles
CREATE OR REPLACE FUNCTION cleanup_old_tiles()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    -- Delete tiles that are older than 30 days and not accessed recently
    DELETE FROM offline_map_tiles 
    WHERE created_at < NOW() - INTERVAL '30 days'
    AND last_accessed_at < NOW() - INTERVAL '7 days'
    AND status IN ('COMPLETED', 'FAILED');
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Create function to get cache statistics
CREATE OR REPLACE FUNCTION get_cache_statistics(start_date TIMESTAMP, end_date TIMESTAMP)
RETURNS TABLE(
    total_caches BIGINT,
    completed_caches BIGINT,
    downloading_caches BIGINT,
    failed_caches BIGINT,
    pending_caches BIGINT,
    total_size_bytes BIGINT,
    avg_download_progress DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT as total_caches,
        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)::BIGINT as completed_caches,
        COUNT(CASE WHEN status = 'DOWNLOADING' THEN 1 END)::BIGINT as downloading_caches,
        COUNT(CASE WHEN status = 'FAILED' THEN 1 END)::BIGINT as failed_caches,
        COUNT(CASE WHEN status = 'PENDING' THEN 1 END)::BIGINT as pending_caches,
        COALESCE(SUM(cache_size_bytes), 0)::BIGINT as total_size_bytes,
        COALESCE(AVG(download_progress), 0.0)::DOUBLE PRECISION as avg_download_progress
    FROM offline_map_caches 
    WHERE created_at BETWEEN start_date AND end_date;
END;
$$ LANGUAGE plpgsql;



