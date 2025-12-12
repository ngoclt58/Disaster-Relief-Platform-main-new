-- Align heatmap_configurations table with HeatmapConfiguration entity definition

-- Add min_zoom column (minimum zoom level for tiles)
ALTER TABLE heatmap_configurations
    ADD COLUMN IF NOT EXISTS min_zoom INTEGER;

-- Add max_zoom column (maximum zoom level for tiles)
ALTER TABLE heatmap_configurations
    ADD COLUMN IF NOT EXISTS max_zoom INTEGER;

-- Add tile_size column (tile size in pixels)
ALTER TABLE heatmap_configurations
    ADD COLUMN IF NOT EXISTS tile_size INTEGER;

-- Initialize defaults for existing rows where values are null
UPDATE heatmap_configurations
SET
    min_zoom = COALESCE(min_zoom, 0),
    max_zoom = COALESCE(max_zoom, 22),
    tile_size = COALESCE(tile_size, 256);


