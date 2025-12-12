-- Create heatmap data and visualization tables

-- Heatmap data points table
CREATE TABLE heatmap_data (
    id BIGSERIAL PRIMARY KEY,
    location GEOMETRY(Point, 4326) NOT NULL,
    heatmap_type VARCHAR(50) NOT NULL,
    intensity DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    radius DOUBLE PRECISION NOT NULL,
    category VARCHAR(100),
    metadata JSONB,
    source_id BIGINT,
    source_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create spatial index for heatmap data
CREATE INDEX idx_heatmap_geom ON heatmap_data USING GIST (location);

-- Create indexes for heatmap data
CREATE INDEX idx_heatmap_type ON heatmap_data (heatmap_type);
CREATE INDEX idx_heatmap_created_at ON heatmap_data (created_at);
CREATE INDEX idx_heatmap_intensity ON heatmap_data (intensity);
CREATE INDEX idx_heatmap_source ON heatmap_data (source_id, source_type);

-- Add constraints for heatmap data
ALTER TABLE heatmap_data ADD CONSTRAINT chk_heatmap_type CHECK (heatmap_type IN (
    'DISASTER_IMPACT', 'RESOURCE_DISTRIBUTION', 'RESPONSE_EFFECTIVENESS', 'NEEDS_DENSITY',
    'TASK_CONCENTRATION', 'VOLUNTEER_ACTIVITY', 'INFRASTRUCTURE_DAMAGE', 'POPULATION_DENSITY',
    'EVACUATION_ROUTES', 'EMERGENCY_SERVICES', 'SUPPLY_CHAINS', 'COMMUNICATION_HUBS',
    'MEDICAL_FACILITIES', 'SHELTER_CAPACITY', 'CUSTOM'
));

ALTER TABLE heatmap_data ADD CONSTRAINT chk_intensity_range CHECK (intensity >= 0 AND intensity <= 1);
ALTER TABLE heatmap_data ADD CONSTRAINT chk_weight_positive CHECK (weight > 0);
ALTER TABLE heatmap_data ADD CONSTRAINT chk_radius_positive CHECK (radius > 0);

-- Heatmap configurations table
CREATE TABLE heatmap_configurations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    heatmap_type VARCHAR(50) NOT NULL,
    color_scheme TEXT NOT NULL,
    intensity_range_min DOUBLE PRECISION NOT NULL,
    intensity_range_max DOUBLE PRECISION NOT NULL,
    radius_multiplier DOUBLE PRECISION NOT NULL,
    opacity DOUBLE PRECISION NOT NULL,
    blur_radius DOUBLE PRECISION NOT NULL,
    gradient_stops JSONB,
    aggregation_method VARCHAR(20) NOT NULL,
    time_window_hours INTEGER,
    spatial_resolution_meters DOUBLE PRECISION,
    filter_criteria JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for heatmap configurations
CREATE INDEX idx_heatmap_config_type ON heatmap_configurations (heatmap_type);
CREATE INDEX idx_heatmap_config_active ON heatmap_configurations (is_active);

-- Add constraints for heatmap configurations
ALTER TABLE heatmap_configurations ADD CONSTRAINT chk_config_heatmap_type CHECK (heatmap_type IN (
    'DISASTER_IMPACT', 'RESOURCE_DISTRIBUTION', 'RESPONSE_EFFECTIVENESS', 'NEEDS_DENSITY',
    'TASK_CONCENTRATION', 'VOLUNTEER_ACTIVITY', 'INFRASTRUCTURE_DAMAGE', 'POPULATION_DENSITY',
    'EVACUATION_ROUTES', 'EMERGENCY_SERVICES', 'SUPPLY_CHAINS', 'COMMUNICATION_HUBS',
    'MEDICAL_FACILITIES', 'SHELTER_CAPACITY', 'CUSTOM'
));

ALTER TABLE heatmap_configurations ADD CONSTRAINT chk_intensity_range_config CHECK (
    intensity_range_min >= 0 AND intensity_range_max <= 1 AND intensity_range_min <= intensity_range_max
);

ALTER TABLE heatmap_configurations ADD CONSTRAINT chk_radius_multiplier_positive CHECK (radius_multiplier > 0);
ALTER TABLE heatmap_configurations ADD CONSTRAINT chk_opacity_range CHECK (opacity >= 0 AND opacity <= 1);
ALTER TABLE heatmap_configurations ADD CONSTRAINT chk_blur_radius_positive CHECK (blur_radius >= 0);
ALTER TABLE heatmap_configurations ADD CONSTRAINT chk_aggregation_method CHECK (aggregation_method IN (
    'SUM', 'AVG', 'MAX', 'MIN', 'COUNT'
));

-- Heatmap layers table
CREATE TABLE heatmap_layers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    heatmap_type VARCHAR(50) NOT NULL,
    bounds GEOMETRY(Polygon, 4326) NOT NULL,
    tile_url_template VARCHAR(500) NOT NULL,
    min_zoom INTEGER NOT NULL,
    max_zoom INTEGER NOT NULL,
    tile_size INTEGER NOT NULL,
    data_points_count BIGINT NOT NULL,
    intensity_min DOUBLE PRECISION NOT NULL,
    intensity_max DOUBLE PRECISION NOT NULL,
    intensity_avg DOUBLE PRECISION NOT NULL,
    configuration_id BIGINT REFERENCES heatmap_configurations(id),
    generation_parameters JSONB,
    file_size_bytes BIGINT,
    is_public BOOLEAN NOT NULL DEFAULT false,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create spatial index for heatmap layers
CREATE INDEX idx_heatmap_layer_geom ON heatmap_layers USING GIST (bounds);

-- Create indexes for heatmap layers
CREATE INDEX idx_heatmap_layer_type ON heatmap_layers (heatmap_type);
CREATE INDEX idx_heatmap_layer_created_at ON heatmap_layers (created_at);
CREATE INDEX idx_heatmap_layer_public ON heatmap_layers (is_public);

-- Add constraints for heatmap layers
ALTER TABLE heatmap_layers ADD CONSTRAINT chk_layer_heatmap_type CHECK (heatmap_type IN (
    'DISASTER_IMPACT', 'RESOURCE_DISTRIBUTION', 'RESPONSE_EFFECTIVENESS', 'NEEDS_DENSITY',
    'TASK_CONCENTRATION', 'VOLUNTEER_ACTIVITY', 'INFRASTRUCTURE_DAMAGE', 'POPULATION_DENSITY',
    'EVACUATION_ROUTES', 'EMERGENCY_SERVICES', 'SUPPLY_CHAINS', 'COMMUNICATION_HUBS',
    'MEDICAL_FACILITIES', 'SHELTER_CAPACITY', 'CUSTOM'
));

ALTER TABLE heatmap_layers ADD CONSTRAINT chk_zoom_range CHECK (min_zoom >= 0 AND max_zoom <= 22 AND min_zoom <= max_zoom);
ALTER TABLE heatmap_layers ADD CONSTRAINT chk_tile_size_positive CHECK (tile_size > 0);
ALTER TABLE heatmap_layers ADD CONSTRAINT chk_data_points_positive CHECK (data_points_count >= 0);
ALTER TABLE heatmap_layers ADD CONSTRAINT chk_intensity_range_layer CHECK (
    intensity_min >= 0 AND intensity_max <= 1 AND intensity_min <= intensity_max
);

-- Add comments
COMMENT ON TABLE heatmap_data IS 'Stores heatmap data points for visualization';
COMMENT ON COLUMN heatmap_data.location IS 'Geographic location of the data point';
COMMENT ON COLUMN heatmap_data.heatmap_type IS 'Type of heatmap (disaster impact, resource distribution, etc.)';
COMMENT ON COLUMN heatmap_data.intensity IS 'Intensity value (0.0 to 1.0)';
COMMENT ON COLUMN heatmap_data.weight IS 'Weight of the data point for aggregation';
COMMENT ON COLUMN heatmap_data.radius IS 'Influence radius in meters';
COMMENT ON COLUMN heatmap_data.category IS 'Optional category for grouping data points';
COMMENT ON COLUMN heatmap_data.metadata IS 'Additional data as JSON';
COMMENT ON COLUMN heatmap_data.source_id IS 'ID of the source record (need, task, etc.)';
COMMENT ON COLUMN heatmap_data.source_type IS 'Type of source record';

COMMENT ON TABLE heatmap_configurations IS 'Stores configuration for heatmap generation and visualization';
COMMENT ON COLUMN heatmap_configurations.name IS 'Configuration name';
COMMENT ON COLUMN heatmap_configurations.heatmap_type IS 'Type of heatmap this configuration applies to';
COMMENT ON COLUMN heatmap_configurations.color_scheme IS 'Color scheme configuration as JSON';
COMMENT ON COLUMN heatmap_configurations.intensity_range_min IS 'Minimum intensity value for visualization';
COMMENT ON COLUMN heatmap_configurations.intensity_range_max IS 'Maximum intensity value for visualization';
COMMENT ON COLUMN heatmap_configurations.radius_multiplier IS 'Multiplier for point radius';
COMMENT ON COLUMN heatmap_configurations.opacity IS 'Opacity of the heatmap (0.0 to 1.0)';
COMMENT ON COLUMN heatmap_configurations.blur_radius IS 'Blur radius in pixels';
COMMENT ON COLUMN heatmap_configurations.gradient_stops IS 'Gradient stops as JSON array';
COMMENT ON COLUMN heatmap_configurations.aggregation_method IS 'Method for aggregating data points';
COMMENT ON COLUMN heatmap_configurations.time_window_hours IS 'Time window for data aggregation in hours';
COMMENT ON COLUMN heatmap_configurations.spatial_resolution_meters IS 'Spatial resolution for aggregation in meters';
COMMENT ON COLUMN heatmap_configurations.filter_criteria IS 'Filter criteria as JSON';

COMMENT ON TABLE heatmap_layers IS 'Stores generated heatmap layers for visualization';
COMMENT ON COLUMN heatmap_layers.name IS 'Layer name';
COMMENT ON COLUMN heatmap_layers.heatmap_type IS 'Type of heatmap layer';
COMMENT ON COLUMN heatmap_layers.bounds IS 'Geographic bounds of the layer';
COMMENT ON COLUMN heatmap_layers.tile_url_template IS 'URL template for map tiles';
COMMENT ON COLUMN heatmap_layers.min_zoom IS 'Minimum zoom level for the layer';
COMMENT ON COLUMN heatmap_layers.max_zoom IS 'Maximum zoom level for the layer';
COMMENT ON COLUMN heatmap_layers.tile_size IS 'Tile size in pixels';
COMMENT ON COLUMN heatmap_layers.data_points_count IS 'Number of data points in the layer';
COMMENT ON COLUMN heatmap_layers.intensity_min IS 'Minimum intensity in the layer';
COMMENT ON COLUMN heatmap_layers.intensity_max IS 'Maximum intensity in the layer';
COMMENT ON COLUMN heatmap_layers.intensity_avg IS 'Average intensity in the layer';
COMMENT ON COLUMN heatmap_layers.configuration_id IS 'Reference to heatmap configuration used';
COMMENT ON COLUMN heatmap_layers.generation_parameters IS 'Parameters used for layer generation as JSON';
COMMENT ON COLUMN heatmap_layers.file_size_bytes IS 'Size of the layer file in bytes';
COMMENT ON COLUMN heatmap_layers.is_public IS 'Whether the layer is publicly accessible';
COMMENT ON COLUMN heatmap_layers.expires_at IS 'Optional expiration time for the layer';

-- Insert sample heatmap data
INSERT INTO heatmap_data (location, heatmap_type, intensity, weight, radius, category, source_id, source_type) VALUES
-- Disaster impact data
(ST_SetSRID(ST_MakePoint(-74.0, 40.7), 4326), 'DISASTER_IMPACT', 0.9, 1.5, 1000, 'FLOODING', 1, 'NEED'),
(ST_SetSRID(ST_MakePoint(-74.1, 40.7), 4326), 'DISASTER_IMPACT', 0.7, 1.2, 800, 'FLOODING', 2, 'NEED'),
(ST_SetSRID(ST_MakePoint(-74.0, 40.8), 4326), 'DISASTER_IMPACT', 0.8, 1.3, 900, 'FIRE', 3, 'NEED'),
(ST_SetSRID(ST_MakePoint(-74.1, 40.8), 4326), 'DISASTER_IMPACT', 0.6, 1.1, 700, 'FIRE', 4, 'NEED'),

-- Resource distribution data
(ST_SetSRID(ST_MakePoint(-73.9, 40.7), 4326), 'RESOURCE_DISTRIBUTION', 0.8, 2.0, 1500, 'MEDICAL', 1, 'RESOURCE'),
(ST_SetSRID(ST_MakePoint(-73.8, 40.7), 4326), 'RESOURCE_DISTRIBUTION', 0.6, 1.5, 1200, 'FOOD', 2, 'RESOURCE'),
(ST_SetSRID(ST_MakePoint(-73.9, 40.8), 4326), 'RESOURCE_DISTRIBUTION', 0.7, 1.8, 1300, 'SHELTER', 3, 'RESOURCE'),
(ST_SetSRID(ST_MakePoint(-73.8, 40.8), 4326), 'RESOURCE_DISTRIBUTION', 0.9, 2.2, 1600, 'EMERGENCY', 4, 'RESOURCE'),

-- Response effectiveness data
(ST_SetSRID(ST_MakePoint(-74.0, 40.6), 4326), 'RESPONSE_EFFECTIVENESS', 0.7, 1.4, 1000, 'RESCUE', 1, 'TASK'),
(ST_SetSRID(ST_MakePoint(-74.1, 40.6), 4326), 'RESPONSE_EFFECTIVENESS', 0.8, 1.6, 1100, 'RESCUE', 2, 'TASK'),
(ST_SetSRID(ST_MakePoint(-74.0, 40.5), 4326), 'RESPONSE_EFFECTIVENESS', 0.6, 1.2, 900, 'RELIEF', 3, 'TASK'),
(ST_SetSRID(ST_MakePoint(-74.1, 40.5), 4326), 'RESPONSE_EFFECTIVENESS', 0.9, 1.8, 1200, 'RELIEF', 4, 'TASK'),

-- Needs density data
(ST_SetSRID(ST_MakePoint(-73.9, 40.6), 4326), 'NEEDS_DENSITY', 0.8, 1.3, 800, 'CRITICAL', 5, 'NEED'),
(ST_SetSRID(ST_MakePoint(-73.8, 40.6), 4326), 'NEEDS_DENSITY', 0.6, 1.1, 700, 'HIGH', 6, 'NEED'),
(ST_SetSRID(ST_MakePoint(-73.9, 40.5), 4326), 'NEEDS_DENSITY', 0.7, 1.2, 750, 'MEDIUM', 7, 'NEED'),
(ST_SetSRID(ST_MakePoint(-73.8, 40.5), 4326), 'NEEDS_DENSITY', 0.5, 1.0, 600, 'LOW', 8, 'NEED');

-- Insert sample heatmap configurations
INSERT INTO heatmap_configurations (
    name, description, heatmap_type, color_scheme, intensity_range_min, intensity_range_max,
    radius_multiplier, opacity, blur_radius, gradient_stops, aggregation_method,
    time_window_hours, spatial_resolution_meters, is_active
) VALUES
(
    'Disaster Impact Heatmap',
    'Configuration for disaster impact visualization',
    'DISASTER_IMPACT',
    '{"colors": ["#00ff00", "#ffff00", "#ff8800", "#ff0000"]}',
    0.0, 1.0, 1.0, 0.7, 20.0,
    '[{"offset": 0, "color": "#00000000"}, {"offset": 0.5, "color": "#ff0000ff"}, {"offset": 1, "color": "#ffff00ff"}]',
    'SUM', 24, 100.0, true
),
(
    'Resource Distribution Heatmap',
    'Configuration for resource distribution visualization',
    'RESOURCE_DISTRIBUTION',
    '{"colors": ["#0000ff", "#0088ff", "#00ffff", "#88ff00"]}',
    0.0, 1.0, 1.2, 0.6, 15.0,
    '[{"offset": 0, "color": "#00000000"}, {"offset": 0.5, "color": "#0000ffff"}, {"offset": 1, "color": "#88ff00ff"}]',
    'AVG', 12, 150.0, true
),
(
    'Response Effectiveness Heatmap',
    'Configuration for response effectiveness visualization',
    'RESPONSE_EFFECTIVENESS',
    '{"colors": ["#ff0000", "#ff8800", "#ffff00", "#00ff00"]}',
    0.0, 1.0, 0.8, 0.8, 25.0,
    '[{"offset": 0, "color": "#00000000"}, {"offset": 0.5, "color": "#ff8800ff"}, {"offset": 1, "color": "#00ff00ff"}]',
    'AVG', 6, 80.0, true
),
(
    'Needs Density Heatmap',
    'Configuration for needs density visualization',
    'NEEDS_DENSITY',
    '{"colors": ["#8800ff", "#ff00ff", "#ff0088", "#ff0000"]}',
    0.0, 1.0, 1.1, 0.65, 18.0,
    '[{"offset": 0, "color": "#00000000"}, {"offset": 0.5, "color": "#ff00ffff"}, {"offset": 1, "color": "#ff0000ff"}]',
    'SUM', 48, 120.0, true
);

-- Create functions for heatmap processing

-- Function to calculate heatmap intensity based on distance
CREATE OR REPLACE FUNCTION calculate_heatmap_intensity(
    point_lon DOUBLE PRECISION,
    point_lat DOUBLE PRECISION,
    target_lon DOUBLE PRECISION,
    target_lat DOUBLE PRECISION,
    intensity DOUBLE PRECISION,
    radius DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
DECLARE
    distance DOUBLE PRECISION;
    normalized_distance DOUBLE PRECISION;
    calculated_intensity DOUBLE PRECISION;
BEGIN
    -- Calculate distance between points
    distance := ST_Distance(
        ST_SetSRID(ST_MakePoint(point_lon, point_lat), 4326)::geography,
        ST_SetSRID(ST_MakePoint(target_lon, target_lat), 4326)::geography
    );
    
    -- Normalize distance to radius
    normalized_distance := distance / radius;
    
    -- Calculate intensity based on distance (Gaussian-like falloff)
    IF normalized_distance >= 1.0 THEN
        calculated_intensity := 0.0;
    ELSE
        calculated_intensity := intensity * EXP(-2 * normalized_distance * normalized_distance);
    END IF;
    
    RETURN calculated_intensity;
END;
$$ LANGUAGE plpgsql;

-- Function to aggregate heatmap data points
CREATE OR REPLACE FUNCTION aggregate_heatmap_data(
    target_lon DOUBLE PRECISION,
    target_lat DOUBLE PRECISION,
    search_radius DOUBLE PRECISION,
    heatmap_type_filter VARCHAR(50)
) RETURNS TABLE(
    aggregated_intensity DOUBLE PRECISION,
    point_count BIGINT,
    avg_weight DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        SUM(calculate_heatmap_intensity(
            ST_X(hd.location), ST_Y(hd.location),
            target_lon, target_lat,
            hd.intensity, hd.radius
        )) as aggregated_intensity,
        COUNT(*) as point_count,
        AVG(hd.weight) as avg_weight
    FROM heatmap_data hd
    WHERE hd.heatmap_type = heatmap_type_filter
    AND ST_DWithin(
        hd.location::geography,
        ST_SetSRID(ST_MakePoint(target_lon, target_lat), 4326)::geography,
        search_radius
    );
END;
$$ LANGUAGE plpgsql;

-- Function to get heatmap statistics for a type
CREATE OR REPLACE FUNCTION get_heatmap_type_statistics(
    heatmap_type_filter VARCHAR(50),
    start_date TIMESTAMP,
    end_date TIMESTAMP
) RETURNS TABLE(
    point_count BIGINT,
    avg_intensity DOUBLE PRECISION,
    min_intensity DOUBLE PRECISION,
    max_intensity DOUBLE PRECISION,
    intensity_stddev DOUBLE PRECISION,
    avg_weight DOUBLE PRECISION,
    avg_radius DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as point_count,
        AVG(hd.intensity) as avg_intensity,
        MIN(hd.intensity) as min_intensity,
        MAX(hd.intensity) as max_intensity,
        STDDEV(hd.intensity) as intensity_stddev,
        AVG(hd.weight) as avg_weight,
        AVG(hd.radius) as avg_radius
    FROM heatmap_data hd
    WHERE hd.heatmap_type = heatmap_type_filter
    AND hd.created_at BETWEEN start_date AND end_date;
END;
$$ LANGUAGE plpgsql;

-- Create views for common heatmap queries

-- View for heatmap data summary
CREATE VIEW heatmap_data_summary AS
SELECT 
    hd.heatmap_type,
    COUNT(*) as point_count,
    AVG(hd.intensity) as avg_intensity,
    MIN(hd.intensity) as min_intensity,
    MAX(hd.intensity) as max_intensity,
    AVG(hd.weight) as avg_weight,
    AVG(hd.radius) as avg_radius,
    MIN(hd.created_at) as earliest_created,
    MAX(hd.created_at) as latest_created
FROM heatmap_data hd
GROUP BY hd.heatmap_type;

-- View for heatmap layer summary
CREATE VIEW heatmap_layer_summary AS
SELECT 
    hl.heatmap_type,
    COUNT(*) as layer_count,
    AVG(hl.data_points_count) as avg_data_points,
    AVG(hl.intensity_max) as avg_max_intensity,
    AVG(hl.file_size_bytes) as avg_file_size,
    MIN(hl.created_at) as earliest_created,
    MAX(hl.created_at) as latest_created
FROM heatmap_layers hl
GROUP BY hl.heatmap_type;



