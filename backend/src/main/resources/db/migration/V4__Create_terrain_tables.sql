-- Create terrain analysis tables for 3D terrain analysis and elevation data

-- Elevation points table
CREATE TABLE elevation_points (
    id BIGSERIAL PRIMARY KEY,
    location GEOMETRY(Point, 4326) NOT NULL,
    elevation DOUBLE PRECISION NOT NULL,
    source VARCHAR(50) NOT NULL,
    accuracy DOUBLE PRECISION,
    resolution DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create spatial index for elevation points
CREATE INDEX idx_elevation_geom ON elevation_points USING GIST (location);

-- Create indexes for elevation points
CREATE INDEX idx_elevation_source ON elevation_points (source);
CREATE INDEX idx_elevation_accuracy ON elevation_points (accuracy);

-- Add constraints
ALTER TABLE elevation_points ADD CONSTRAINT chk_elevation_positive CHECK (elevation >= -1000 AND elevation <= 10000);
ALTER TABLE elevation_points ADD CONSTRAINT chk_accuracy_positive CHECK (accuracy IS NULL OR accuracy > 0);
ALTER TABLE elevation_points ADD CONSTRAINT chk_resolution_positive CHECK (resolution IS NULL OR resolution > 0);

-- Terrain analysis table
CREATE TABLE terrain_analysis (
    id BIGSERIAL PRIMARY KEY,
    area GEOMETRY(Polygon, 4326) NOT NULL,
    analysis_type VARCHAR(50) NOT NULL,
    min_elevation DOUBLE PRECISION,
    max_elevation DOUBLE PRECISION,
    avg_elevation DOUBLE PRECISION,
    elevation_variance DOUBLE PRECISION,
    slope_avg DOUBLE PRECISION,
    slope_max DOUBLE PRECISION,
    aspect_avg DOUBLE PRECISION,
    roughness_index DOUBLE PRECISION,
    accessibility_score DOUBLE PRECISION,
    flood_risk_score DOUBLE PRECISION,
    analysis_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create spatial index for terrain analysis
CREATE INDEX idx_terrain_geom ON terrain_analysis USING GIST (area);

-- Create indexes for terrain analysis
CREATE INDEX idx_terrain_type ON terrain_analysis (analysis_type);

-- Add constraints for terrain analysis
ALTER TABLE terrain_analysis ADD CONSTRAINT chk_analysis_type CHECK (analysis_type IN (
    'ROUTING', 'ACCESSIBILITY', 'FLOOD_RISK', 'EMERGENCY_RESPONSE', 'INFRASTRUCTURE', 'GENERAL'
));

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_elevation_range CHECK (
    min_elevation IS NULL OR (min_elevation >= -1000 AND min_elevation <= 10000)
);

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_max_elevation_range CHECK (
    max_elevation IS NULL OR (max_elevation >= -1000 AND max_elevation <= 10000)
);

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_avg_elevation_range CHECK (
    avg_elevation IS NULL OR (avg_elevation >= -1000 AND avg_elevation <= 10000)
);

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_slope_range CHECK (
    slope_avg IS NULL OR (slope_avg >= 0 AND slope_avg <= 90)
);

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_slope_max_range CHECK (
    slope_max IS NULL OR (slope_max >= 0 AND slope_max <= 90)
);

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_aspect_range CHECK (
    aspect_avg IS NULL OR (aspect_avg >= 0 AND aspect_avg < 360)
);

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_roughness_positive CHECK (
    roughness_index IS NULL OR roughness_index >= 0
);

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_accessibility_score CHECK (
    accessibility_score IS NULL OR (accessibility_score >= 0 AND accessibility_score <= 1)
);

ALTER TABLE terrain_analysis ADD CONSTRAINT chk_flood_risk_score CHECK (
    flood_risk_score IS NULL OR (flood_risk_score >= 0 AND flood_risk_score <= 1)
);

-- Add comments
COMMENT ON TABLE elevation_points IS 'Stores elevation data points for terrain analysis';
COMMENT ON COLUMN elevation_points.location IS 'Geographic location of the elevation point';
COMMENT ON COLUMN elevation_points.elevation IS 'Elevation in meters above sea level';
COMMENT ON COLUMN elevation_points.source IS 'Source of the elevation data (SRTM, ASTER, LIDAR, etc.)';
COMMENT ON COLUMN elevation_points.accuracy IS 'Accuracy of the elevation measurement in meters';
COMMENT ON COLUMN elevation_points.resolution IS 'Resolution of the elevation data in meters per pixel';

COMMENT ON TABLE terrain_analysis IS 'Stores terrain analysis results for specific areas';
COMMENT ON COLUMN terrain_analysis.area IS 'Geographic area covered by the analysis';
COMMENT ON COLUMN terrain_analysis.analysis_type IS 'Type of terrain analysis performed';
COMMENT ON COLUMN terrain_analysis.min_elevation IS 'Minimum elevation in the area (meters)';
COMMENT ON COLUMN terrain_analysis.max_elevation IS 'Maximum elevation in the area (meters)';
COMMENT ON COLUMN terrain_analysis.avg_elevation IS 'Average elevation in the area (meters)';
COMMENT ON COLUMN terrain_analysis.elevation_variance IS 'Variance of elevation in the area';
COMMENT ON COLUMN terrain_analysis.slope_avg IS 'Average slope in the area (degrees)';
COMMENT ON COLUMN terrain_analysis.slope_max IS 'Maximum slope in the area (degrees)';
COMMENT ON COLUMN terrain_analysis.aspect_avg IS 'Average aspect in the area (degrees, 0-360)';
COMMENT ON COLUMN terrain_analysis.roughness_index IS 'Terrain roughness index';
COMMENT ON COLUMN terrain_analysis.accessibility_score IS 'Accessibility score (0-1, higher is more accessible)';
COMMENT ON COLUMN terrain_analysis.flood_risk_score IS 'Flood risk score (0-1, higher is more flood-prone)';
COMMENT ON COLUMN terrain_analysis.analysis_data IS 'Additional analysis results as JSON';

-- Insert sample elevation data (SRTM-like data for demonstration)
INSERT INTO elevation_points (location, elevation, source, accuracy, resolution) VALUES
-- Sample points around a hypothetical disaster area
(ST_SetSRID(ST_MakePoint(-74.0, 40.7), 4326), 10.5, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-74.1, 40.7), 4326), 15.2, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-74.0, 40.8), 4326), 8.7, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-74.1, 40.8), 4326), 12.3, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-73.9, 40.7), 4326), 25.8, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-73.9, 40.8), 4326), 18.4, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-74.0, 40.6), 4326), 5.2, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-74.1, 40.6), 4326), 7.8, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-73.8, 40.7), 4326), 35.6, 'SRTM', 16.0, 30.0),
(ST_SetSRID(ST_MakePoint(-73.8, 40.8), 4326), 28.9, 'SRTM', 16.0, 30.0);

-- Create a function to calculate slope between two elevation points
CREATE OR REPLACE FUNCTION calculate_slope(
    lon1 DOUBLE PRECISION, lat1 DOUBLE PRECISION, elev1 DOUBLE PRECISION,
    lon2 DOUBLE PRECISION, lat2 DOUBLE PRECISION, elev2 DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
DECLARE
    distance DOUBLE PRECISION;
    elevation_diff DOUBLE PRECISION;
BEGIN
    -- Calculate horizontal distance using Haversine formula
    distance := ST_Distance(
        ST_SetSRID(ST_MakePoint(lon1, lat1), 4326)::geography,
        ST_SetSRID(ST_MakePoint(lon2, lat2), 4326)::geography
    );
    
    -- Calculate elevation difference
    elevation_diff := elev2 - elev1;
    
    -- Return slope in degrees
    IF distance = 0 THEN
        RETURN 0;
    ELSE
        RETURN degrees(atan(elevation_diff / distance));
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Create a function to calculate aspect between two points
CREATE OR REPLACE FUNCTION calculate_aspect(
    lon1 DOUBLE PRECISION, lat1 DOUBLE PRECISION,
    lon2 DOUBLE PRECISION, lat2 DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
DECLARE
    delta_lon DOUBLE PRECISION;
    lat1_rad DOUBLE PRECISION;
    lat2_rad DOUBLE PRECISION;
    y DOUBLE PRECISION;
    x DOUBLE PRECISION;
    aspect DOUBLE PRECISION;
BEGIN
    delta_lon := radians(lon2 - lon1);
    lat1_rad := radians(lat1);
    lat2_rad := radians(lat2);
    
    y := sin(delta_lon) * cos(lat2_rad);
    x := cos(lat1_rad) * sin(lat2_rad) - sin(lat1_rad) * cos(lat2_rad) * cos(delta_lon);
    
    aspect := degrees(atan2(y, x));
    RETURN (aspect + 360) % 360; -- Normalize to 0-360
END;
$$ LANGUAGE plpgsql;

-- Create a function to get elevation at a point (interpolated)
CREATE OR REPLACE FUNCTION get_elevation_at_point(
    target_lon DOUBLE PRECISION, target_lat DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
DECLARE
    nearest_point RECORD;
    distance DOUBLE PRECISION;
BEGIN
    -- Find the nearest elevation point
    SELECT 
        elevation,
        ST_Distance(location, ST_SetSRID(ST_MakePoint(target_lon, target_lat), 4326)::geography) as dist
    INTO nearest_point
    FROM elevation_points
    ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint(target_lon, target_lat), 4326)::geography)
    LIMIT 1;
    
    -- Return the elevation of the nearest point
    -- In a real implementation, you might want to do interpolation
    RETURN nearest_point.elevation;
END;
$$ LANGUAGE plpgsql;



