-- Create location history analytics tables for tracking movement patterns
-- This migration creates tables for location history, patterns, and optimizations

-- Create location_history table
CREATE TABLE location_history (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    entity_name VARCHAR(255),
    position GEOMETRY(Point, 4326) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    altitude DOUBLE PRECISION,
    heading DOUBLE PRECISION,
    speed DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    accuracy DOUBLE PRECISION NOT NULL DEFAULT 10.0,
    activity_type VARCHAR(50) NOT NULL,
    activity_description TEXT,
    timestamp TIMESTAMP NOT NULL,
    duration_seconds INTEGER,
    distance_from_previous DOUBLE PRECISION,
    is_stationary BOOLEAN NOT NULL DEFAULT false,
    is_significant BOOLEAN NOT NULL DEFAULT false,
    location_context JSONB,
    environmental_conditions JSONB,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for location_history
CREATE INDEX idx_location_history_entity ON location_history(entity_type, entity_id);
CREATE INDEX idx_location_history_timestamp ON location_history(timestamp);
CREATE INDEX idx_location_history_geom ON location_history USING GIST(position);
CREATE INDEX idx_location_history_activity ON location_history(activity_type);
CREATE INDEX idx_location_history_created ON location_history(created_at);
CREATE INDEX idx_location_history_stationary ON location_history(is_stationary);
CREATE INDEX idx_location_history_significant ON location_history(is_significant);
CREATE INDEX idx_location_history_speed ON location_history(speed);
CREATE INDEX idx_location_history_accuracy ON location_history(accuracy);

-- Create location_patterns table
CREATE TABLE location_patterns (
    id BIGSERIAL PRIMARY KEY,
    location_history_id BIGINT REFERENCES location_history(id) ON DELETE SET NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    pattern_type VARCHAR(50) NOT NULL,
    pattern_name VARCHAR(255) NOT NULL,
    pattern_description TEXT,
    pattern_geometry GEOMETRY(Geometry, 4326),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_seconds BIGINT NOT NULL,
    distance_meters DOUBLE PRECISION NOT NULL,
    average_speed DOUBLE PRECISION NOT NULL,
    max_speed DOUBLE PRECISION NOT NULL,
    confidence_score DOUBLE PRECISION NOT NULL DEFAULT 0.5,
    frequency INTEGER NOT NULL DEFAULT 1,
    is_recurring BOOLEAN NOT NULL DEFAULT false,
    is_optimal BOOLEAN NOT NULL DEFAULT false,
    optimization_suggestions JSONB,
    pattern_characteristics JSONB,
    environmental_factors JSONB,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for location_patterns
CREATE INDEX idx_location_pattern_entity ON location_patterns(entity_type, entity_id);
CREATE INDEX idx_location_pattern_type ON location_patterns(pattern_type);
CREATE INDEX idx_location_pattern_confidence ON location_patterns(confidence_score);
CREATE INDEX idx_location_pattern_geom ON location_patterns USING GIST(pattern_geometry);
CREATE INDEX idx_location_pattern_created ON location_patterns(created_at);
CREATE INDEX idx_location_pattern_recurring ON location_patterns(is_recurring);
CREATE INDEX idx_location_pattern_optimal ON location_patterns(is_optimal);
CREATE INDEX idx_location_pattern_frequency ON location_patterns(frequency);
CREATE INDEX idx_location_pattern_speed ON location_patterns(average_speed);
CREATE INDEX idx_location_pattern_distance ON location_patterns(distance_meters);

-- Create location_optimizations table
CREATE TABLE location_optimizations (
    id BIGSERIAL PRIMARY KEY,
    location_pattern_id BIGINT NOT NULL REFERENCES location_patterns(id) ON DELETE CASCADE,
    optimization_type VARCHAR(50) NOT NULL,
    optimization_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    suggested_route GEOMETRY(LineString, 4326),
    current_efficiency DOUBLE PRECISION NOT NULL,
    projected_efficiency DOUBLE PRECISION NOT NULL,
    time_savings_seconds BIGINT,
    distance_savings_meters DOUBLE PRECISION,
    resource_savings JSONB,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    implementation_difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    estimated_implementation_time INTEGER,
    cost_benefit_ratio DOUBLE PRECISION,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    affected_entities JSONB,
    implementation_steps JSONB,
    success_metrics JSONB,
    monitoring_requirements JSONB,
    is_implemented BOOLEAN NOT NULL DEFAULT false,
    implementation_date TIMESTAMP,
    implementation_notes TEXT,
    actual_efficiency_gain DOUBLE PRECISION,
    feedback TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for location_optimizations
CREATE INDEX idx_location_optimization_pattern ON location_optimizations(location_pattern_id);
CREATE INDEX idx_location_optimization_type ON location_optimizations(optimization_type);
CREATE INDEX idx_location_optimization_priority ON location_optimizations(priority);
CREATE INDEX idx_location_optimization_status ON location_optimizations(status);
CREATE INDEX idx_location_optimization_created ON location_optimizations(created_at);
CREATE INDEX idx_location_optimization_implemented ON location_optimizations(is_implemented);
CREATE INDEX idx_location_optimization_difficulty ON location_optimizations(implementation_difficulty);
CREATE INDEX idx_location_optimization_risk ON location_optimizations(risk_level);
CREATE INDEX idx_location_optimization_efficiency ON location_optimizations(current_efficiency);
CREATE INDEX idx_location_optimization_projected ON location_optimizations(projected_efficiency);

-- Add comments to tables
COMMENT ON TABLE location_history IS 'Location history for tracking movement patterns';
COMMENT ON TABLE location_patterns IS 'Detected movement patterns from location history';
COMMENT ON TABLE location_optimizations IS 'Optimization suggestions based on movement patterns';

-- Add comments to key columns
COMMENT ON COLUMN location_history.position IS 'Geographic position of the entity';
COMMENT ON COLUMN location_history.speed IS 'Speed in meters per second';
COMMENT ON COLUMN location_history.accuracy IS 'Position accuracy in meters';
COMMENT ON COLUMN location_history.distance_from_previous IS 'Distance from previous location in meters';
COMMENT ON COLUMN location_history.is_stationary IS 'Whether entity is stationary at this location';
COMMENT ON COLUMN location_history.is_significant IS 'Whether this is a significant location point';
COMMENT ON COLUMN location_patterns.pattern_geometry IS 'Geometry representing the pattern';
COMMENT ON COLUMN location_patterns.confidence_score IS 'Pattern confidence score (0.0 to 1.0)';
COMMENT ON COLUMN location_patterns.frequency IS 'How often this pattern occurs';
COMMENT ON COLUMN location_patterns.is_recurring IS 'Whether this pattern is recurring';
COMMENT ON COLUMN location_patterns.is_optimal IS 'Whether this pattern is optimal';
COMMENT ON COLUMN location_optimizations.suggested_route IS 'Suggested optimized route geometry';
COMMENT ON COLUMN location_optimizations.current_efficiency IS 'Current efficiency score (0.0 to 1.0)';
COMMENT ON COLUMN location_optimizations.projected_efficiency IS 'Projected efficiency after optimization';
COMMENT ON COLUMN location_optimizations.time_savings_seconds IS 'Projected time savings in seconds';
COMMENT ON COLUMN location_optimizations.distance_savings_meters IS 'Projected distance savings in meters';

-- Create function to calculate distance between two points
CREATE OR REPLACE FUNCTION calculate_distance_meters(
    lat1 DOUBLE PRECISION,
    lon1 DOUBLE PRECISION,
    lat2 DOUBLE PRECISION,
    lon2 DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
BEGIN
    RETURN ST_Distance(
        ST_SetSRID(ST_MakePoint(lon1, lat1), 4326)::geography,
        ST_SetSRID(ST_MakePoint(lon2, lat2), 4326)::geography
    );
END;
$$ LANGUAGE plpgsql;

-- Create function to update distance from previous location
CREATE OR REPLACE FUNCTION update_distance_from_previous()
RETURNS TRIGGER AS $$
DECLARE
    prev_location RECORD;
BEGIN
    -- Get the previous location for this entity
    SELECT latitude, longitude
    INTO prev_location
    FROM location_history
    WHERE entity_type = NEW.entity_type
    AND entity_id = NEW.entity_id
    AND id < NEW.id
    ORDER BY timestamp DESC
    LIMIT 1;
    
    -- Calculate distance if previous location exists
    IF FOUND THEN
        NEW.distance_from_previous := calculate_distance_meters(
            prev_location.latitude,
            prev_location.longitude,
            NEW.latitude,
            NEW.longitude
        );
    ELSE
        NEW.distance_from_previous := 0.0;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to update distance from previous location
CREATE TRIGGER trigger_update_distance_from_previous
    BEFORE INSERT ON location_history
    FOR EACH ROW
    EXECUTE FUNCTION update_distance_from_previous();

-- Create function to detect stationary locations
CREATE OR REPLACE FUNCTION detect_stationary_locations()
RETURNS TRIGGER AS $$
BEGIN
    -- Mark as stationary if speed is very low and duration is significant
    IF NEW.speed < 0.5 AND NEW.duration_seconds > 300 THEN
        NEW.is_stationary := true;
    ELSE
        NEW.is_stationary := false;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to detect stationary locations
CREATE TRIGGER trigger_detect_stationary_locations
    BEFORE INSERT ON location_history
    FOR EACH ROW
    EXECUTE FUNCTION detect_stationary_locations();

-- Create function to detect significant locations
CREATE OR REPLACE FUNCTION detect_significant_locations()
RETURNS TRIGGER AS $$
BEGIN
    -- Mark as significant if distance from previous is large
    IF NEW.distance_from_previous > 100 THEN
        NEW.is_significant := true;
    ELSE
        NEW.is_significant := false;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to detect significant locations
CREATE TRIGGER trigger_detect_significant_locations
    BEFORE INSERT ON location_history
    FOR EACH ROW
    EXECUTE FUNCTION detect_significant_locations();

-- Create function to get location history statistics
CREATE OR REPLACE FUNCTION get_location_history_stats(
    start_time TIMESTAMP,
    end_time TIMESTAMP
) RETURNS TABLE(
    total_locations BIGINT,
    stationary_locations BIGINT,
    significant_locations BIGINT,
    avg_speed DOUBLE PRECISION,
    max_speed DOUBLE PRECISION,
    avg_accuracy DOUBLE PRECISION,
    unique_entity_types BIGINT,
    unique_entities BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT as total_locations,
        COUNT(CASE WHEN is_stationary = true THEN 1 END)::BIGINT as stationary_locations,
        COUNT(CASE WHEN is_significant = true THEN 1 END)::BIGINT as significant_locations,
        AVG(speed)::DOUBLE PRECISION as avg_speed,
        MAX(speed)::DOUBLE PRECISION as max_speed,
        AVG(accuracy)::DOUBLE PRECISION as avg_accuracy,
        COUNT(DISTINCT entity_type)::BIGINT as unique_entity_types,
        COUNT(DISTINCT entity_id)::BIGINT as unique_entities
    FROM location_history 
    WHERE timestamp BETWEEN start_time AND end_time;
END;
$$ LANGUAGE plpgsql;

-- Create function to get pattern statistics
CREATE OR REPLACE FUNCTION get_pattern_stats(
    start_date TIMESTAMP,
    end_date TIMESTAMP
) RETURNS TABLE(
    total_patterns BIGINT,
    linear_patterns BIGINT,
    circular_patterns BIGINT,
    stationary_patterns BIGINT,
    route_patterns BIGINT,
    search_patterns BIGINT,
    recurring_patterns BIGINT,
    optimal_patterns BIGINT,
    avg_confidence DOUBLE PRECISION,
    avg_speed DOUBLE PRECISION,
    avg_distance DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT as total_patterns,
        COUNT(CASE WHEN pattern_type = 'LINEAR_MOVEMENT' THEN 1 END)::BIGINT as linear_patterns,
        COUNT(CASE WHEN pattern_type = 'CIRCULAR_MOVEMENT' THEN 1 END)::BIGINT as circular_patterns,
        COUNT(CASE WHEN pattern_type = 'STATIONARY_CLUSTER' THEN 1 END)::BIGINT as stationary_patterns,
        COUNT(CASE WHEN pattern_type = 'COMMUTE_ROUTE' THEN 1 END)::BIGINT as route_patterns,
        COUNT(CASE WHEN pattern_type = 'SEARCH_GRID' THEN 1 END)::BIGINT as search_patterns,
        COUNT(CASE WHEN is_recurring = true THEN 1 END)::BIGINT as recurring_patterns,
        COUNT(CASE WHEN is_optimal = true THEN 1 END)::BIGINT as optimal_patterns,
        AVG(confidence_score)::DOUBLE PRECISION as avg_confidence,
        AVG(average_speed)::DOUBLE PRECISION as avg_speed,
        AVG(distance_meters)::DOUBLE PRECISION as avg_distance
    FROM location_patterns 
    WHERE created_at BETWEEN start_date AND end_date;
END;
$$ LANGUAGE plpgsql;

-- Create function to get optimization statistics
CREATE OR REPLACE FUNCTION get_optimization_stats(
    start_date TIMESTAMP,
    end_date TIMESTAMP
) RETURNS TABLE(
    total_optimizations BIGINT,
    pending_optimizations BIGINT,
    approved_optimizations BIGINT,
    in_progress_optimizations BIGINT,
    completed_optimizations BIGINT,
    implemented_optimizations BIGINT,
    implemented_count BIGINT,
    avg_current_efficiency DOUBLE PRECISION,
    avg_projected_efficiency DOUBLE PRECISION,
    avg_actual_efficiency_gain DOUBLE PRECISION,
    total_time_savings BIGINT,
    total_distance_savings DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT as total_optimizations,
        COUNT(CASE WHEN status = 'PENDING' THEN 1 END)::BIGINT as pending_optimizations,
        COUNT(CASE WHEN status = 'APPROVED' THEN 1 END)::BIGINT as approved_optimizations,
        COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END)::BIGINT as in_progress_optimizations,
        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)::BIGINT as completed_optimizations,
        COUNT(CASE WHEN status = 'IMPLEMENTED' THEN 1 END)::BIGINT as implemented_optimizations,
        COUNT(CASE WHEN is_implemented = true THEN 1 END)::BIGINT as implemented_count,
        AVG(current_efficiency)::DOUBLE PRECISION as avg_current_efficiency,
        AVG(projected_efficiency)::DOUBLE PRECISION as avg_projected_efficiency,
        AVG(COALESCE(actual_efficiency_gain, 0))::DOUBLE PRECISION as avg_actual_efficiency_gain,
        SUM(COALESCE(time_savings_seconds, 0))::BIGINT as total_time_savings,
        SUM(COALESCE(distance_savings_meters, 0))::DOUBLE PRECISION as total_distance_savings
    FROM location_optimizations 
    WHERE created_at BETWEEN start_date AND end_date;
END;
$$ LANGUAGE plpgsql;



