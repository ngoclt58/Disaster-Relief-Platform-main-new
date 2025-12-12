-- Create satellite imagery and damage assessment tables

-- Satellite images table
CREATE TABLE satellite_images (
    id BIGSERIAL PRIMARY KEY,
    image_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    coverage_area GEOMETRY(Polygon, 4326) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    satellite_name VARCHAR(100),
    captured_at TIMESTAMP NOT NULL,
    resolution_meters DOUBLE PRECISION NOT NULL,
    cloud_cover_percentage DOUBLE PRECISION,
    sun_elevation_angle DOUBLE PRECISION,
    sun_azimuth_angle DOUBLE PRECISION,
    image_bands JSONB,
    metadata JSONB,
    processing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    quality_score DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create spatial index for satellite images
CREATE INDEX idx_satellite_geom ON satellite_images USING GIST (coverage_area);

-- Create indexes for satellite images
CREATE INDEX idx_satellite_provider ON satellite_images (provider);
CREATE INDEX idx_satellite_captured_at ON satellite_images (captured_at);
CREATE INDEX idx_satellite_resolution ON satellite_images (resolution_meters);
CREATE INDEX idx_satellite_processing_status ON satellite_images (processing_status);

-- Add constraints for satellite images
ALTER TABLE satellite_images ADD CONSTRAINT chk_provider CHECK (provider IN (
    'LANDSAT', 'SENTINEL', 'MODIS', 'SPOT', 'WORLDVIEW', 'PLEIADES', 
    'KOMPSAT', 'PLANET', 'MAXAR', 'CUSTOM'
));

ALTER TABLE satellite_images ADD CONSTRAINT chk_processing_status CHECK (processing_status IN (
    'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'ARCHIVED'
));

ALTER TABLE satellite_images ADD CONSTRAINT chk_resolution_positive CHECK (resolution_meters > 0);
ALTER TABLE satellite_images ADD CONSTRAINT chk_cloud_cover_range CHECK (cloud_cover_percentage IS NULL OR (cloud_cover_percentage >= 0 AND cloud_cover_percentage <= 100));
ALTER TABLE satellite_images ADD CONSTRAINT chk_sun_elevation_range CHECK (sun_elevation_angle IS NULL OR (sun_elevation_angle >= -90 AND sun_elevation_angle <= 90));
ALTER TABLE satellite_images ADD CONSTRAINT chk_sun_azimuth_range CHECK (sun_azimuth_angle IS NULL OR (sun_azimuth_angle >= 0 AND sun_azimuth_angle < 360));
ALTER TABLE satellite_images ADD CONSTRAINT chk_quality_score_range CHECK (quality_score IS NULL OR (quality_score >= 0 AND quality_score <= 1));

-- Damage assessments table
CREATE TABLE damage_assessments (
    id BIGSERIAL PRIMARY KEY,
    satellite_image_id BIGINT NOT NULL REFERENCES satellite_images(id) ON DELETE CASCADE,
    damage_area GEOMETRY(Polygon, 4326) NOT NULL,
    damage_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    confidence_score DOUBLE PRECISION NOT NULL,
    damage_percentage DOUBLE PRECISION,
    affected_area_sqm DOUBLE PRECISION,
    pre_disaster_image_id BIGINT REFERENCES satellite_images(id),
    change_detection_score DOUBLE PRECISION,
    analysis_algorithm VARCHAR(100),
    analysis_parameters JSONB,
    assessed_at TIMESTAMP NOT NULL,
    assessed_by VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create spatial index for damage assessments
CREATE INDEX idx_damage_geom ON damage_assessments USING GIST (damage_area);

-- Create indexes for damage assessments
CREATE INDEX idx_damage_type ON damage_assessments (damage_type);
CREATE INDEX idx_damage_severity ON damage_assessments (severity);
CREATE INDEX idx_damage_assessed_at ON damage_assessments (assessed_at);
CREATE INDEX idx_damage_satellite_image ON damage_assessments (satellite_image_id);
CREATE INDEX idx_damage_confidence ON damage_assessments (confidence_score);

-- Add constraints for damage assessments
ALTER TABLE damage_assessments ADD CONSTRAINT chk_damage_type CHECK (damage_type IN (
    'BUILDING_COLLAPSE', 'FLOODING', 'FIRE', 'LANDSLIDE', 'DEBRIS', 
    'INFRASTRUCTURE', 'VEGETATION', 'EROSION', 'CONTAMINATION', 'GENERAL'
));

ALTER TABLE damage_assessments ADD CONSTRAINT chk_severity CHECK (severity IN (
    'MINIMAL', 'LIGHT', 'MODERATE', 'SEVERE', 'CATASTROPHIC'
));

ALTER TABLE damage_assessments ADD CONSTRAINT chk_confidence_score_range CHECK (confidence_score >= 0 AND confidence_score <= 1);
ALTER TABLE damage_assessments ADD CONSTRAINT chk_damage_percentage_range CHECK (damage_percentage IS NULL OR (damage_percentage >= 0 AND damage_percentage <= 100));
ALTER TABLE damage_assessments ADD CONSTRAINT chk_affected_area_positive CHECK (affected_area_sqm IS NULL OR affected_area_sqm >= 0);
ALTER TABLE damage_assessments ADD CONSTRAINT chk_change_detection_score_range CHECK (change_detection_score IS NULL OR (change_detection_score >= 0 AND change_detection_score <= 1));

-- Add comments
COMMENT ON TABLE satellite_images IS 'Stores satellite imagery data for damage assessment';
COMMENT ON COLUMN satellite_images.image_url IS 'URL to the full resolution satellite image';
COMMENT ON COLUMN satellite_images.thumbnail_url IS 'URL to the thumbnail image';
COMMENT ON COLUMN satellite_images.coverage_area IS 'Geographic area covered by the satellite image';
COMMENT ON COLUMN satellite_images.provider IS 'Satellite imagery provider (LANDSAT, SENTINEL, etc.)';
COMMENT ON COLUMN satellite_images.satellite_name IS 'Name of the satellite that captured the image';
COMMENT ON COLUMN satellite_images.captured_at IS 'Date and time when the image was captured';
COMMENT ON COLUMN satellite_images.resolution_meters IS 'Spatial resolution of the image in meters per pixel';
COMMENT ON COLUMN satellite_images.cloud_cover_percentage IS 'Percentage of the image covered by clouds';
COMMENT ON COLUMN satellite_images.sun_elevation_angle IS 'Sun elevation angle at capture time (degrees)';
COMMENT ON COLUMN satellite_images.sun_azimuth_angle IS 'Sun azimuth angle at capture time (degrees)';
COMMENT ON COLUMN satellite_images.image_bands IS 'Available spectral bands as JSON array';
COMMENT ON COLUMN satellite_images.metadata IS 'Additional satellite metadata as JSON';
COMMENT ON COLUMN satellite_images.processing_status IS 'Current processing status of the image';
COMMENT ON COLUMN satellite_images.quality_score IS 'Overall quality score of the image (0-1)';

COMMENT ON TABLE damage_assessments IS 'Stores damage assessment results from satellite imagery analysis';
COMMENT ON COLUMN damage_assessments.satellite_image_id IS 'Reference to the satellite image used for assessment';
COMMENT ON COLUMN damage_assessments.damage_area IS 'Geographic area where damage was detected';
COMMENT ON COLUMN damage_assessments.damage_type IS 'Type of damage detected';
COMMENT ON COLUMN damage_assessments.severity IS 'Severity level of the damage';
COMMENT ON COLUMN damage_assessments.confidence_score IS 'Confidence score for the damage assessment (0-1)';
COMMENT ON COLUMN damage_assessments.damage_percentage IS 'Percentage of the area that is damaged (0-100)';
COMMENT ON COLUMN damage_assessments.affected_area_sqm IS 'Total area affected by damage in square meters';
COMMENT ON COLUMN damage_assessments.pre_disaster_image_id IS 'Reference to pre-disaster image for comparison';
COMMENT ON COLUMN damage_assessments.change_detection_score IS 'Confidence score for change detection (0-1)';
COMMENT ON COLUMN damage_assessments.analysis_algorithm IS 'Algorithm used for damage analysis';
COMMENT ON COLUMN damage_assessments.analysis_parameters IS 'Parameters used for the analysis algorithm as JSON';
COMMENT ON COLUMN damage_assessments.assessed_at IS 'Date and time when the assessment was performed';
COMMENT ON COLUMN damage_assessments.assessed_by IS 'User or system that performed the assessment';

-- Insert sample satellite image data
INSERT INTO satellite_images (
    image_url, thumbnail_url, coverage_area, provider, satellite_name, 
    captured_at, resolution_meters, cloud_cover_percentage, 
    sun_elevation_angle, sun_azimuth_angle, image_bands, metadata, 
    processing_status, quality_score
) VALUES
(
    'https://example.com/satellite/image1.tif',
    'https://example.com/satellite/thumb1.jpg',
    ST_MakeEnvelope(-74.1, 40.6, -73.9, 40.8, 4326),
    'LANDSAT',
    'Landsat-8',
    '2024-01-15 10:30:00',
    30.0,
    15.5,
    45.2,
    180.5,
    '["B2", "B3", "B4", "B5", "B6", "B7", "B8", "B10", "B11"]',
    '{"mission": "landsat-8", "path": 15, "row": 32, "acquisition_mode": "NOMINAL"}',
    'COMPLETED',
    0.85
),
(
    'https://example.com/satellite/image2.tif',
    'https://example.com/satellite/thumb2.jpg',
    ST_MakeEnvelope(-74.0, 40.7, -73.8, 40.9, 4326),
    'SENTINEL',
    'Sentinel-2A',
    '2024-01-16 11:15:00',
    10.0,
    8.2,
    42.8,
    185.3,
    '["B02", "B03", "B04", "B08", "B11", "B12"]',
    '{"mission": "sentinel-2", "tile": "18TYN", "acquisition_mode": "NOMINAL"}',
    'COMPLETED',
    0.92
),
(
    'https://example.com/satellite/image3.tif',
    'https://example.com/satellite/thumb3.jpg',
    ST_MakeEnvelope(-74.2, 40.5, -74.0, 40.7, 4326),
    'WORLDVIEW',
    'WorldView-3',
    '2024-01-17 09:45:00',
    0.5,
    5.1,
    48.7,
    175.2,
    '["Blue", "Green", "Red", "NIR1", "NIR2", "RedEdge", "Coastal", "Yellow", "RedEdge2", "NIR3", "NIR4", "SWIR1", "SWIR2", "SWIR3", "SWIR4", "SWIR5", "SWIR6", "SWIR7", "SWIR8"]',
    '{"mission": "worldview-3", "acquisition_mode": "NOMINAL", "off_nadir_angle": 12.5}',
    'COMPLETED',
    0.95
);

-- Insert sample damage assessment data
INSERT INTO damage_assessments (
    satellite_image_id, damage_area, damage_type, severity, confidence_score,
    damage_percentage, affected_area_sqm, analysis_algorithm, assessed_at, 
    assessed_by, notes
) VALUES
(
    1,
    ST_MakeEnvelope(-74.05, 40.65, -74.0, 40.7, 4326),
    'BUILDING_COLLAPSE',
    'MODERATE',
    0.85,
    45.0,
    50000.0,
    'NDVI_CHANGE_DETECTION',
    '2024-01-15 14:30:00',
    'damage_assessment_system',
    'Automated damage detection using NDVI change analysis'
),
(
    2,
    ST_MakeEnvelope(-73.95, 40.75, -73.9, 40.8, 4326),
    'FLOODING',
    'SEVERE',
    0.92,
    75.0,
    120000.0,
    'WATER_INDEX_ANALYSIS',
    '2024-01-16 15:45:00',
    'damage_assessment_system',
    'Flood damage detected using water index analysis'
),
(
    3,
    ST_MakeEnvelope(-74.15, 40.55, -74.1, 40.6, 4326),
    'FIRE',
    'CATASTROPHIC',
    0.88,
    90.0,
    80000.0,
    'THERMAL_ANOMALY_DETECTION',
    '2024-01-17 16:20:00',
    'damage_assessment_system',
    'Fire damage detected using thermal anomaly analysis'
);

-- Create functions for satellite imagery analysis

-- Function to calculate image quality score
CREATE OR REPLACE FUNCTION calculate_image_quality_score(
    cloud_cover DOUBLE PRECISION,
    resolution DOUBLE PRECISION,
    sun_elevation DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
DECLARE
    score DOUBLE PRECISION := 1.0;
BEGIN
    -- Penalize high cloud cover
    IF cloud_cover > 50 THEN
        score := score - 0.5;
    ELSIF cloud_cover > 25 THEN
        score := score - 0.3;
    ELSIF cloud_cover > 10 THEN
        score := score - 0.1;
    END IF;
    
    -- Penalize low resolution
    IF resolution > 30 THEN
        score := score - 0.3;
    ELSIF resolution > 10 THEN
        score := score - 0.1;
    END IF;
    
    -- Penalize low sun elevation
    IF sun_elevation < 20 THEN
        score := score - 0.2;
    END IF;
    
    RETURN GREATEST(0.0, LEAST(1.0, score));
END;
$$ LANGUAGE plpgsql;

-- Function to determine damage severity from percentage
CREATE OR REPLACE FUNCTION determine_damage_severity(
    damage_percentage DOUBLE PRECISION
) RETURNS VARCHAR(20) AS $$
BEGIN
    IF damage_percentage >= 81 THEN
        RETURN 'CATASTROPHIC';
    ELSIF damage_percentage >= 61 THEN
        RETURN 'SEVERE';
    ELSIF damage_percentage >= 41 THEN
        RETURN 'MODERATE';
    ELSIF damage_percentage >= 21 THEN
        RETURN 'LIGHT';
    ELSE
        RETURN 'MINIMAL';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate affected area in square meters
CREATE OR REPLACE FUNCTION calculate_affected_area_sqm(
    damage_area GEOMETRY
) RETURNS DOUBLE PRECISION AS $$
DECLARE
    area_degrees DOUBLE PRECISION;
    area_sqm DOUBLE PRECISION;
BEGIN
    -- Calculate area in square degrees
    area_degrees := ST_Area(damage_area);
    
    -- Convert to square meters (approximate)
    area_sqm := area_degrees * 111000 * 111000;
    
    RETURN area_sqm;
END;
$$ LANGUAGE plpgsql;

-- Create view for satellite image summary
CREATE VIEW satellite_image_summary AS
SELECT 
    si.id,
    si.provider,
    si.satellite_name,
    si.captured_at,
    si.resolution_meters,
    si.cloud_cover_percentage,
    si.quality_score,
    si.processing_status,
    COUNT(da.id) as damage_assessment_count,
    AVG(da.confidence_score) as avg_damage_confidence
FROM satellite_images si
LEFT JOIN damage_assessments da ON si.id = da.satellite_image_id
GROUP BY si.id, si.provider, si.satellite_name, si.captured_at, 
         si.resolution_meters, si.cloud_cover_percentage, si.quality_score, si.processing_status;

-- Create view for damage assessment summary
CREATE VIEW damage_assessment_summary AS
SELECT 
    da.id,
    da.damage_type,
    da.severity,
    da.confidence_score,
    da.damage_percentage,
    da.affected_area_sqm,
    da.assessed_at,
    da.assessed_by,
    si.provider as image_provider,
    si.satellite_name,
    si.captured_at as image_captured_at
FROM damage_assessments da
JOIN satellite_images si ON da.satellite_image_id = si.id;



