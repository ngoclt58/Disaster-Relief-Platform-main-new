-- Create indoor navigation tables for GPS-denied environments
-- This migration creates tables for indoor maps, nodes, edges, positions, routes, and zones

-- Create indoor_maps table
CREATE TABLE indoor_maps (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    facility_id BIGINT NOT NULL,
    facility_name VARCHAR(255) NOT NULL,
    floor_number INTEGER NOT NULL,
    floor_name VARCHAR(255),
    map_bounds GEOMETRY(Geometry, 4326) NOT NULL,
    map_type VARCHAR(50) NOT NULL,
    coordinate_system VARCHAR(50) NOT NULL DEFAULT 'local',
    scale_factor DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    map_image_url VARCHAR(500),
    map_data JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for indoor_maps
CREATE INDEX idx_indoor_map_name ON indoor_maps(name);
CREATE INDEX idx_indoor_map_facility ON indoor_maps(facility_id);
CREATE INDEX idx_indoor_map_floor ON indoor_maps(floor_number);
CREATE INDEX idx_indoor_map_active ON indoor_maps(is_active);
CREATE INDEX idx_indoor_map_bounds ON indoor_maps USING GIST(map_bounds);
CREATE INDEX idx_indoor_map_type ON indoor_maps(map_type);

-- Create indoor_nodes table
CREATE TABLE indoor_nodes (
    id BIGSERIAL PRIMARY KEY,
    indoor_map_id BIGINT NOT NULL REFERENCES indoor_maps(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    position GEOMETRY(Point, 4326) NOT NULL,
    local_x DOUBLE PRECISION NOT NULL,
    local_y DOUBLE PRECISION NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    is_accessible BOOLEAN NOT NULL DEFAULT true,
    accessibility_features JSONB,
    capacity INTEGER,
    current_occupancy INTEGER DEFAULT 0,
    is_emergency_exit BOOLEAN NOT NULL DEFAULT false,
    is_elevator BOOLEAN NOT NULL DEFAULT false,
    is_stairs BOOLEAN NOT NULL DEFAULT false,
    floor_level INTEGER,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(indoor_map_id, node_id)
);

-- Create indexes for indoor_nodes
CREATE INDEX idx_indoor_node_map ON indoor_nodes(indoor_map_id);
CREATE INDEX idx_indoor_node_type ON indoor_nodes(node_type);
CREATE INDEX idx_indoor_node_geom ON indoor_nodes USING GIST(position);
CREATE INDEX idx_indoor_node_accessible ON indoor_nodes(is_accessible);
CREATE INDEX idx_indoor_node_floor ON indoor_nodes(floor_level);
CREATE INDEX idx_indoor_node_emergency ON indoor_nodes(is_emergency_exit);
CREATE INDEX idx_indoor_node_elevator ON indoor_nodes(is_elevator);
CREATE INDEX idx_indoor_node_stairs ON indoor_nodes(is_stairs);

-- Create indoor_edges table
CREATE TABLE indoor_edges (
    id BIGSERIAL PRIMARY KEY,
    indoor_map_id BIGINT NOT NULL REFERENCES indoor_maps(id) ON DELETE CASCADE,
    from_node_id BIGINT NOT NULL REFERENCES indoor_nodes(id) ON DELETE CASCADE,
    to_node_id BIGINT NOT NULL REFERENCES indoor_nodes(id) ON DELETE CASCADE,
    edge_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    path GEOMETRY(LineString, 4326),
    edge_type VARCHAR(50) NOT NULL,
    is_accessible BOOLEAN NOT NULL DEFAULT true,
    is_bidirectional BOOLEAN NOT NULL DEFAULT true,
    distance DOUBLE PRECISION NOT NULL,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    max_speed DOUBLE PRECISION,
    accessibility_features JSONB,
    is_emergency_route BOOLEAN NOT NULL DEFAULT false,
    is_restricted BOOLEAN NOT NULL DEFAULT false,
    restriction_type VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(indoor_map_id, edge_id)
);

-- Create indexes for indoor_edges
CREATE INDEX idx_indoor_edge_map ON indoor_edges(indoor_map_id);
CREATE INDEX idx_indoor_edge_from ON indoor_edges(from_node_id);
CREATE INDEX idx_indoor_edge_to ON indoor_edges(to_node_id);
CREATE INDEX idx_indoor_edge_type ON indoor_edges(edge_type);
CREATE INDEX idx_indoor_edge_accessible ON indoor_edges(is_accessible);
CREATE INDEX idx_indoor_edge_bidirectional ON indoor_edges(is_bidirectional);
CREATE INDEX idx_indoor_edge_emergency ON indoor_edges(is_emergency_route);
CREATE INDEX idx_indoor_edge_restricted ON indoor_edges(is_restricted);
CREATE INDEX idx_indoor_edge_path ON indoor_edges USING GIST(path);

-- Create indoor_zones table
CREATE TABLE indoor_zones (
    id BIGSERIAL PRIMARY KEY,
    indoor_map_id BIGINT NOT NULL REFERENCES indoor_maps(id) ON DELETE CASCADE,
    zone_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    geometry GEOMETRY(Geometry, 4326) NOT NULL,
    zone_type VARCHAR(50) NOT NULL,
    is_accessible BOOLEAN NOT NULL DEFAULT true,
    capacity INTEGER,
    current_occupancy INTEGER DEFAULT 0,
    is_restricted BOOLEAN NOT NULL DEFAULT false,
    restriction_type VARCHAR(100),
    access_level VARCHAR(50),
    is_emergency_shelter BOOLEAN NOT NULL DEFAULT false,
    is_medical_facility BOOLEAN NOT NULL DEFAULT false,
    is_evacuation_zone BOOLEAN NOT NULL DEFAULT false,
    floor_level INTEGER,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(indoor_map_id, zone_id)
);

-- Create indexes for indoor_zones
CREATE INDEX idx_indoor_zone_map ON indoor_zones(indoor_map_id);
CREATE INDEX idx_indoor_zone_type ON indoor_zones(zone_type);
CREATE INDEX idx_indoor_zone_geom ON indoor_zones USING GIST(geometry);
CREATE INDEX idx_indoor_zone_accessible ON indoor_zones(is_accessible);
CREATE INDEX idx_indoor_zone_floor ON indoor_zones(floor_level);
CREATE INDEX idx_indoor_zone_emergency ON indoor_zones(is_emergency_shelter);
CREATE INDEX idx_indoor_zone_medical ON indoor_zones(is_medical_facility);
CREATE INDEX idx_indoor_zone_evacuation ON indoor_zones(is_evacuation_zone);

-- Create indoor_positions table
CREATE TABLE indoor_positions (
    id BIGSERIAL PRIMARY KEY,
    indoor_map_id BIGINT NOT NULL REFERENCES indoor_maps(id) ON DELETE CASCADE,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    entity_name VARCHAR(255),
    position GEOMETRY(Point, 4326) NOT NULL,
    local_x DOUBLE PRECISION NOT NULL,
    local_y DOUBLE PRECISION NOT NULL,
    floor_level INTEGER NOT NULL,
    heading DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    accuracy DOUBLE PRECISION NOT NULL,
    positioning_method VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_valid BOOLEAN NOT NULL DEFAULT true,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for indoor_positions
CREATE INDEX idx_indoor_position_map ON indoor_positions(indoor_map_id);
CREATE INDEX idx_indoor_position_entity ON indoor_positions(entity_type, entity_id);
CREATE INDEX idx_indoor_position_geom ON indoor_positions USING GIST(position);
CREATE INDEX idx_indoor_position_timestamp ON indoor_positions(timestamp);
CREATE INDEX idx_indoor_position_floor ON indoor_positions(floor_level);
CREATE INDEX idx_indoor_position_method ON indoor_positions(positioning_method);
CREATE INDEX idx_indoor_position_valid ON indoor_positions(is_valid);

-- Create indoor_routes table
CREATE TABLE indoor_routes (
    id BIGSERIAL PRIMARY KEY,
    indoor_map_id BIGINT NOT NULL REFERENCES indoor_maps(id) ON DELETE CASCADE,
    from_node_id BIGINT NOT NULL REFERENCES indoor_nodes(id) ON DELETE CASCADE,
    to_node_id BIGINT NOT NULL REFERENCES indoor_nodes(id) ON DELETE CASCADE,
    route_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    path GEOMETRY(LineString, 4326) NOT NULL,
    route_type VARCHAR(50) NOT NULL,
    total_distance DOUBLE PRECISION NOT NULL,
    estimated_time INTEGER NOT NULL,
    difficulty_level VARCHAR(20) NOT NULL,
    is_accessible BOOLEAN NOT NULL DEFAULT true,
    is_emergency_route BOOLEAN NOT NULL DEFAULT false,
    is_restricted BOOLEAN NOT NULL DEFAULT false,
    access_level VARCHAR(50),
    waypoints JSONB,
    instructions JSONB,
    metadata JSONB,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(indoor_map_id, route_id)
);

-- Create indexes for indoor_routes
CREATE INDEX idx_indoor_route_map ON indoor_routes(indoor_map_id);
CREATE INDEX idx_indoor_route_from ON indoor_routes(from_node_id);
CREATE INDEX idx_indoor_route_to ON indoor_routes(to_node_id);
CREATE INDEX idx_indoor_route_type ON indoor_routes(route_type);
CREATE INDEX idx_indoor_route_accessible ON indoor_routes(is_accessible);
CREATE INDEX idx_indoor_route_emergency ON indoor_routes(is_emergency_route);
CREATE INDEX idx_indoor_route_restricted ON indoor_routes(is_restricted);
CREATE INDEX idx_indoor_route_difficulty ON indoor_routes(difficulty_level);
CREATE INDEX idx_indoor_route_created_at ON indoor_routes(created_at);
CREATE INDEX idx_indoor_route_path ON indoor_routes USING GIST(path);

-- Create indoor_route_steps table
CREATE TABLE indoor_route_steps (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES indoor_routes(id) ON DELETE CASCADE,
    sequence_number INTEGER NOT NULL,
    instruction TEXT NOT NULL,
    position GEOMETRY(Point, 4326) NOT NULL,
    local_x DOUBLE PRECISION NOT NULL,
    local_y DOUBLE PRECISION NOT NULL,
    distance_from_start DOUBLE PRECISION NOT NULL,
    estimated_time_from_start INTEGER NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_direction VARCHAR(50),
    landmark VARCHAR(255),
    floor_level INTEGER NOT NULL,
    is_critical BOOLEAN NOT NULL DEFAULT false,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for indoor_route_steps
CREATE INDEX idx_indoor_route_step_route ON indoor_route_steps(route_id);
CREATE INDEX idx_indoor_route_step_sequence ON indoor_route_steps(sequence_number);
CREATE INDEX idx_indoor_route_step_geom ON indoor_route_steps USING GIST(position);
CREATE INDEX idx_indoor_route_step_floor ON indoor_route_steps(floor_level);
CREATE INDEX idx_indoor_route_step_action ON indoor_route_steps(action_type);
CREATE INDEX idx_indoor_route_step_critical ON indoor_route_steps(is_critical);

-- Add comments to tables
COMMENT ON TABLE indoor_maps IS 'Indoor maps for GPS-denied environments';
COMMENT ON TABLE indoor_nodes IS 'Navigation nodes within indoor maps';
COMMENT ON TABLE indoor_edges IS 'Connections between navigation nodes';
COMMENT ON TABLE indoor_zones IS 'Areas or zones within indoor maps';
COMMENT ON TABLE indoor_positions IS 'Recorded positions within indoor maps';
COMMENT ON TABLE indoor_routes IS 'Calculated navigation routes';
COMMENT ON TABLE indoor_route_steps IS 'Step-by-step instructions for routes';

-- Add comments to key columns
COMMENT ON COLUMN indoor_maps.map_bounds IS 'Geographic bounds of the indoor map';
COMMENT ON COLUMN indoor_maps.scale_factor IS 'Pixels per meter for coordinate conversion';
COMMENT ON COLUMN indoor_nodes.position IS 'Geographic position of the node';
COMMENT ON COLUMN indoor_nodes.local_x IS 'Local X coordinate within the map';
COMMENT ON COLUMN indoor_nodes.local_y IS 'Local Y coordinate within the map';
COMMENT ON COLUMN indoor_edges.path IS 'Optional path geometry for the edge';
COMMENT ON COLUMN indoor_edges.weight IS 'Navigation weight (lower = preferred)';
COMMENT ON COLUMN indoor_zones.geometry IS 'Geographic geometry of the zone';
COMMENT ON COLUMN indoor_positions.position IS 'Geographic position of the entity';
COMMENT ON COLUMN indoor_positions.accuracy IS 'Position accuracy in meters';
COMMENT ON COLUMN indoor_routes.path IS 'Route path geometry';
COMMENT ON COLUMN indoor_routes.waypoints IS 'JSON array of waypoint coordinates';
COMMENT ON COLUMN indoor_routes.instructions IS 'JSON array of turn-by-turn instructions';



