-- Create geofencing and monitoring tables
-- Note: This migration replaces the simple geofences table from V1__baseline.sql

-- Drop old tables, views, and functions if they exist (from previous migrations or manual creation)
DROP VIEW IF EXISTS active_geofence_alerts CASCADE;
DROP VIEW IF EXISTS active_geofence_events CASCADE;
DROP VIEW IF EXISTS geofence_summary CASCADE;

DROP FUNCTION IF EXISTS get_geofence_alert_statistics(TIMESTAMP, TIMESTAMP) CASCADE;
DROP FUNCTION IF EXISTS get_geofence_event_statistics(BIGINT, TIMESTAMP, TIMESTAMP) CASCADE;
DROP FUNCTION IF EXISTS get_geofence_statistics(TIMESTAMP, TIMESTAMP) CASCADE;
DROP FUNCTION IF EXISTS check_point_in_geofences(DOUBLE PRECISION, DOUBLE PRECISION) CASCADE;

DROP TABLE IF EXISTS geofence_alerts CASCADE;
DROP TABLE IF EXISTS geofence_events CASCADE;
DROP TABLE IF EXISTS geofences CASCADE;

-- Geofences table
CREATE TABLE geofences (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    boundary GEOMETRY(Geometry, 4326) NOT NULL,
    geofence_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    buffer_distance_meters DOUBLE PRECISION,
    check_interval_seconds INTEGER NOT NULL DEFAULT 300,
    alert_threshold INTEGER NOT NULL DEFAULT 1,
    cooldown_period_seconds INTEGER NOT NULL DEFAULT 3600,
    notification_channels JSONB,
    auto_actions JSONB,
    metadata JSONB,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_checked_at TIMESTAMP,
    last_alert_at TIMESTAMP
);

-- Create spatial index for geofences
CREATE INDEX idx_geofence_geom ON geofences USING GIST (boundary);

-- Create indexes for geofences
CREATE INDEX idx_geofence_name ON geofences (name);
CREATE INDEX idx_geofence_active ON geofences (is_active);
CREATE INDEX idx_geofence_created_at ON geofences (created_at);
CREATE INDEX idx_geofence_type ON geofences (geofence_type);
CREATE INDEX idx_geofence_priority ON geofences (priority);

-- Add constraints for geofences
ALTER TABLE geofences ADD CONSTRAINT chk_geofence_type CHECK (geofence_type IN (
    'DISASTER_ZONE', 'EVACUATION_ZONE', 'RESTRICTED_ZONE', 'RESOURCE_DEPOT',
    'EMERGENCY_SHELTER', 'MEDICAL_FACILITY', 'COMMUNICATION_HUB', 'SUPPLY_ROUTE',
    'INFRASTRUCTURE', 'POPULATION_DENSITY', 'VULNERABLE_AREA', 'RESPONSE_BASE',
    'CHECKPOINT', 'QUARANTINE_ZONE', 'RECOVERY_ZONE', 'CUSTOM'
));

ALTER TABLE geofences ADD CONSTRAINT chk_priority CHECK (priority IN (
    'CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'
));

ALTER TABLE geofences ADD CONSTRAINT chk_check_interval_positive CHECK (check_interval_seconds > 0);
ALTER TABLE geofences ADD CONSTRAINT chk_alert_threshold_positive CHECK (alert_threshold > 0);
ALTER TABLE geofences ADD CONSTRAINT chk_cooldown_period_positive CHECK (cooldown_period_seconds > 0);

-- Geofence events table
CREATE TABLE geofence_events (
    id BIGSERIAL PRIMARY KEY,
    geofence_id BIGINT NOT NULL REFERENCES geofences(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    entity_name VARCHAR(200),
    event_data JSONB,
    severity VARCHAR(20) NOT NULL,
    confidence_score DOUBLE PRECISION,
    occurred_at TIMESTAMP NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    is_processed BOOLEAN NOT NULL DEFAULT false,
    processing_notes TEXT
);

-- Create spatial index for geofence events
CREATE INDEX idx_geofence_event_geom ON geofence_events USING GIST (location);

-- Create indexes for geofence events
CREATE INDEX idx_geofence_event_geofence ON geofence_events (geofence_id);
CREATE INDEX idx_geofence_event_type ON geofence_events (event_type);
CREATE INDEX idx_geofence_event_occurred_at ON geofence_events (occurred_at);
CREATE INDEX idx_geofence_event_entity ON geofence_events (entity_type, entity_id);
CREATE INDEX idx_geofence_event_processed ON geofence_events (is_processed);

-- Add constraints for geofence events
ALTER TABLE geofence_events ADD CONSTRAINT chk_event_type CHECK (event_type IN (
    'ENTRY', 'EXIT', 'DWELL', 'PROXIMITY', 'VIOLATION', 'EMERGENCY',
    'RESOURCE_DEPLETION', 'CAPACITY_EXCEEDED', 'MAINTENANCE_REQUIRED',
    'STATUS_CHANGE', 'CUSTOM'
));

ALTER TABLE geofence_events ADD CONSTRAINT chk_severity CHECK (severity IN (
    'CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'
));

ALTER TABLE geofence_events ADD CONSTRAINT chk_confidence_score_range CHECK (
    confidence_score IS NULL OR (confidence_score >= 0 AND confidence_score <= 1)
);

-- Geofence alerts table
CREATE TABLE geofence_alerts (
    id BIGSERIAL PRIMARY KEY,
    geofence_id BIGINT NOT NULL REFERENCES geofences(id) ON DELETE CASCADE,
    alert_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    triggered_by_event_id BIGINT REFERENCES geofence_events(id),
    alert_data JSONB,
    notification_channels JSONB,
    auto_actions_triggered JSONB,
    assigned_to VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    acknowledged_by VARCHAR(100),
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),
    resolution_notes TEXT,
    escalated_at TIMESTAMP,
    escalated_to VARCHAR(100),
    escalation_reason TEXT
);

-- Create indexes for geofence alerts
CREATE INDEX idx_geofence_alert_geofence ON geofence_alerts (geofence_id);
CREATE INDEX idx_geofence_alert_type ON geofence_alerts (alert_type);
CREATE INDEX idx_geofence_alert_status ON geofence_alerts (status);
CREATE INDEX idx_geofence_alert_created_at ON geofence_alerts (created_at);
CREATE INDEX idx_geofence_alert_severity ON geofence_alerts (severity);
CREATE INDEX idx_geofence_alert_assigned ON geofence_alerts (assigned_to);

-- Add constraints for geofence alerts
ALTER TABLE geofence_alerts ADD CONSTRAINT chk_alert_type CHECK (alert_type IN (
    'BOUNDARY_VIOLATION', 'CAPACITY_EXCEEDED', 'RESOURCE_DEPLETION', 'EMERGENCY_DETECTED',
    'UNAUTHORIZED_ACCESS', 'MAINTENANCE_REQUIRED', 'STATUS_CHANGE', 'THRESHOLD_EXCEEDED',
    'TIME_BASED_ALERT', 'CUSTOM'
));

ALTER TABLE geofence_alerts ADD CONSTRAINT chk_alert_severity CHECK (severity IN (
    'CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'
));

ALTER TABLE geofence_alerts ADD CONSTRAINT chk_alert_status CHECK (status IN (
    'ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS', 'RESOLVED', 'ESCALATED', 'CANCELLED', 'EXPIRED'
));

-- Add comments
COMMENT ON TABLE geofences IS 'Stores geofence boundaries for automated monitoring and alerts';
COMMENT ON COLUMN geofences.name IS 'Name of the geofence';
COMMENT ON COLUMN geofences.description IS 'Description of the geofence';
COMMENT ON COLUMN geofences.boundary IS 'Geographic boundary of the geofence';
COMMENT ON COLUMN geofences.geofence_type IS 'Type of geofence (disaster zone, evacuation zone, etc.)';
COMMENT ON COLUMN geofences.priority IS 'Priority level for monitoring and alerts';
COMMENT ON COLUMN geofences.is_active IS 'Whether the geofence is active for monitoring';
COMMENT ON COLUMN geofences.buffer_distance_meters IS 'Buffer distance around the geofence in meters';
COMMENT ON COLUMN geofences.check_interval_seconds IS 'Interval between geofence checks in seconds';
COMMENT ON COLUMN geofences.alert_threshold IS 'Number of events before triggering an alert';
COMMENT ON COLUMN geofences.cooldown_period_seconds IS 'Cooldown period between alerts in seconds';
COMMENT ON COLUMN geofences.notification_channels IS 'Channels for sending notifications as JSON array';
COMMENT ON COLUMN geofences.auto_actions IS 'Automated actions to trigger as JSON array';
COMMENT ON COLUMN geofences.metadata IS 'Additional configuration data as JSON';

COMMENT ON TABLE geofence_events IS 'Stores events that occur within geofences';
COMMENT ON COLUMN geofence_events.geofence_id IS 'Reference to the geofence where the event occurred';
COMMENT ON COLUMN geofence_events.event_type IS 'Type of event (entry, exit, violation, etc.)';
COMMENT ON COLUMN geofence_events.location IS 'Geographic location where the event occurred';
COMMENT ON COLUMN geofence_events.entity_type IS 'Type of entity that triggered the event';
COMMENT ON COLUMN geofence_events.entity_id IS 'ID of the entity that triggered the event';
COMMENT ON COLUMN geofence_events.entity_name IS 'Name or identifier of the entity';
COMMENT ON COLUMN geofence_events.event_data IS 'Additional event data as JSON';
COMMENT ON COLUMN geofence_events.severity IS 'Severity level of the event';
COMMENT ON COLUMN geofence_events.confidence_score IS 'Confidence in the event detection (0-1)';
COMMENT ON COLUMN geofence_events.occurred_at IS 'When the event actually occurred';
COMMENT ON COLUMN geofence_events.detected_at IS 'When the event was detected by the system';
COMMENT ON COLUMN geofence_events.processed_at IS 'When the event was processed';
COMMENT ON COLUMN geofence_events.is_processed IS 'Whether the event has been processed';
COMMENT ON COLUMN geofence_events.processing_notes IS 'Notes about event processing';

COMMENT ON TABLE geofence_alerts IS 'Stores alerts generated by geofence monitoring';
COMMENT ON COLUMN geofence_alerts.geofence_id IS 'Reference to the geofence that generated the alert';
COMMENT ON COLUMN geofence_alerts.alert_type IS 'Type of alert generated';
COMMENT ON COLUMN geofence_alerts.title IS 'Alert title';
COMMENT ON COLUMN geofence_alerts.message IS 'Alert message';
COMMENT ON COLUMN geofence_alerts.severity IS 'Severity level of the alert';
COMMENT ON COLUMN geofence_alerts.status IS 'Current status of the alert';
COMMENT ON COLUMN geofence_alerts.triggered_by_event_id IS 'ID of the event that triggered this alert';
COMMENT ON COLUMN geofence_alerts.alert_data IS 'Additional alert data as JSON';
COMMENT ON COLUMN geofence_alerts.notification_channels IS 'Channels used for notification as JSON array';
COMMENT ON COLUMN geofence_alerts.auto_actions_triggered IS 'Automated actions that were triggered as JSON array';
COMMENT ON COLUMN geofence_alerts.assigned_to IS 'User or team assigned to handle the alert';
COMMENT ON COLUMN geofence_alerts.acknowledged_at IS 'When the alert was acknowledged';
COMMENT ON COLUMN geofence_alerts.acknowledged_by IS 'Who acknowledged the alert';
COMMENT ON COLUMN geofence_alerts.resolved_at IS 'When the alert was resolved';
COMMENT ON COLUMN geofence_alerts.resolved_by IS 'Who resolved the alert';
COMMENT ON COLUMN geofence_alerts.resolution_notes IS 'Notes about alert resolution';
COMMENT ON COLUMN geofence_alerts.escalated_at IS 'When the alert was escalated';
COMMENT ON COLUMN geofence_alerts.escalated_to IS 'Who the alert was escalated to';
COMMENT ON COLUMN geofence_alerts.escalation_reason IS 'Reason for escalation';

-- Insert sample geofence data
INSERT INTO geofences (
    name, description, boundary, geofence_type, priority, is_active,
    buffer_distance_meters, check_interval_seconds, alert_threshold,
    cooldown_period_seconds, notification_channels, auto_actions,
    created_by
) VALUES
(
    'Disaster Zone Alpha',
    'Primary disaster affected area with high risk',
    ST_SetSRID(ST_MakePolygon(ST_MakeLine(ARRAY[
        ST_MakePoint(-74.1, 40.6),
        ST_MakePoint(-73.9, 40.6),
        ST_MakePoint(-73.9, 40.8),
        ST_MakePoint(-74.1, 40.8),
        ST_MakePoint(-74.1, 40.6)
    ])), 4326),
    'DISASTER_ZONE',
    'CRITICAL',
    true,
    500.0,
    60,
    1,
    1800,
    '["email", "sms", "push"]',
    '["notify_emergency_team", "update_status_board", "send_alert"]',
    'system_admin'
),
(
    'Evacuation Zone Beta',
    'Mandatory evacuation area for safety',
    ST_SetSRID(ST_MakePolygon(ST_MakeLine(ARRAY[
        ST_MakePoint(-74.0, 40.5),
        ST_MakePoint(-73.8, 40.5),
        ST_MakePoint(-73.8, 40.7),
        ST_MakePoint(-74.0, 40.7),
        ST_MakePoint(-74.0, 40.5)
    ])), 4326),
    'EVACUATION_ZONE',
    'HIGH',
    true,
    200.0,
    120,
    2,
    3600,
    '["email", "push"]',
    '["notify_evacuation_team", "update_evacuation_status"]',
    'system_admin'
),
(
    'Resource Depot Gamma',
    'Central resource distribution center',
    ST_SetSRID(ST_MakePolygon(ST_MakeLine(ARRAY[
        ST_MakePoint(-73.9, 40.7),
        ST_MakePoint(-73.7, 40.7),
        ST_MakePoint(-73.7, 40.9),
        ST_MakePoint(-73.9, 40.9),
        ST_MakePoint(-73.9, 40.7)
    ])), 4326),
    'RESOURCE_DEPOT',
    'MEDIUM',
    true,
    100.0,
    300,
    3,
    7200,
    '["email"]',
    '["update_inventory_status", "notify_logistics_team"]',
    'system_admin'
),
(
    'Emergency Shelter Delta',
    'Emergency shelter for displaced persons',
    ST_SetSRID(ST_MakePolygon(ST_MakeLine(ARRAY[
        ST_MakePoint(-74.2, 40.6),
        ST_MakePoint(-74.0, 40.6),
        ST_MakePoint(-74.0, 40.8),
        ST_MakePoint(-74.2, 40.8),
        ST_MakePoint(-74.2, 40.6)
    ])), 4326),
    'EMERGENCY_SHELTER',
    'HIGH',
    true,
    150.0,
    180,
    2,
    3600,
    '["email", "sms"]',
    '["notify_shelter_management", "update_capacity_status"]',
    'system_admin'
);

-- Insert sample geofence events
INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    severity, confidence_score, occurred_at, detected_at, is_processed
) VALUES
(1, 'ENTRY', ST_SetSRID(ST_MakePoint(-74.0, 40.7), 4326), 'PERSON', 1, 'John Doe', 'HIGH', 0.95, NOW(), NOW(), false),
(1, 'VIOLATION', ST_SetSRID(ST_MakePoint(-74.05, 40.65), 4326), 'VEHICLE', 2, 'Vehicle ABC123', 'CRITICAL', 0.98, NOW(), NOW(), false),
(2, 'ENTRY', ST_SetSRID(ST_MakePoint(-73.9, 40.6), 4326), 'PERSON', 3, 'Jane Smith', 'MEDIUM', 0.87, NOW(), NOW(), false),
(3, 'ENTRY', ST_SetSRID(ST_MakePoint(-73.8, 40.8), 4326), 'VEHICLE', 4, 'Supply Truck XYZ789', 'LOW', 0.92, NOW(), NOW(), false),
(4, 'CAPACITY_EXCEEDED', ST_SetSRID(ST_MakePoint(-74.1, 40.7), 4326), 'FACILITY', 5, 'Shelter Alpha', 'HIGH', 0.89, NOW(), NOW(), false);

-- Insert sample geofence alerts
INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, notification_channels, auto_actions_triggered,
    created_at
) VALUES
(1, 'BOUNDARY_VIOLATION', 'Disaster Zone Alpha Alert', 'Unauthorized vehicle entry detected in disaster zone', 'CRITICAL', 'ACTIVE', 2, '["email", "sms", "push"]', '["notify_emergency_team", "update_status_board"]', NOW()),
(4, 'CAPACITY_EXCEEDED', 'Emergency Shelter Delta Alert', 'Shelter capacity exceeded, immediate action required', 'HIGH', 'ACTIVE', 5, '["email", "sms"]', '["notify_shelter_management", "update_capacity_status"]', NOW());

-- Create functions for geofencing operations

-- Function to check if a point is within any geofence
CREATE OR REPLACE FUNCTION check_point_in_geofences(
    point_lon DOUBLE PRECISION,
    point_lat DOUBLE PRECISION
) RETURNS TABLE(
    geofence_id BIGINT,
    geofence_name VARCHAR(100),
    geofence_type VARCHAR(50),
    priority VARCHAR(20)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        g.id,
        g.name,
        g.geofence_type,
        g.priority
    FROM geofences g
    WHERE ST_Contains(g.boundary, ST_SetSRID(ST_MakePoint(point_lon, point_lat), 4326))
    AND g.is_active = true
    ORDER BY 
        CASE g.priority 
            WHEN 'CRITICAL' THEN 1
            WHEN 'HIGH' THEN 2
            WHEN 'MEDIUM' THEN 3
            WHEN 'LOW' THEN 4
            WHEN 'INFO' THEN 5
        END,
        g.created_at DESC;
END;
$$ LANGUAGE plpgsql;

-- Function to get geofence statistics
CREATE OR REPLACE FUNCTION get_geofence_statistics(
    start_date TIMESTAMP,
    end_date TIMESTAMP
) RETURNS TABLE(
    total_geofences BIGINT,
    active_geofences BIGINT,
    disaster_zones BIGINT,
    evacuation_zones BIGINT,
    resource_depots BIGINT,
    critical_geofences BIGINT,
    high_priority_geofences BIGINT,
    avg_minutes_since_last_check DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_geofences,
        COUNT(CASE WHEN is_active = true THEN 1 END) as active_geofences,
        COUNT(CASE WHEN geofence_type = 'DISASTER_ZONE' THEN 1 END) as disaster_zones,
        COUNT(CASE WHEN geofence_type = 'EVACUATION_ZONE' THEN 1 END) as evacuation_zones,
        COUNT(CASE WHEN geofence_type = 'RESOURCE_DEPOT' THEN 1 END) as resource_depots,
        COUNT(CASE WHEN priority = 'CRITICAL' THEN 1 END) as critical_geofences,
        COUNT(CASE WHEN priority = 'HIGH' THEN 1 END) as high_priority_geofences,
        AVG(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - last_checked_at))/60) as avg_minutes_since_last_check
    FROM geofences 
    WHERE created_at BETWEEN start_date AND end_date;
END;
$$ LANGUAGE plpgsql;

-- Function to get geofence event statistics
CREATE OR REPLACE FUNCTION get_geofence_event_statistics(
    geofence_id_param BIGINT,
    start_date TIMESTAMP,
    end_date TIMESTAMP
) RETURNS TABLE(
    total_events BIGINT,
    entry_events BIGINT,
    exit_events BIGINT,
    violation_events BIGINT,
    critical_events BIGINT,
    high_severity_events BIGINT,
    unprocessed_events BIGINT,
    avg_confidence_score DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_events,
        COUNT(CASE WHEN event_type = 'ENTRY' THEN 1 END) as entry_events,
        COUNT(CASE WHEN event_type = 'EXIT' THEN 1 END) as exit_events,
        COUNT(CASE WHEN event_type = 'VIOLATION' THEN 1 END) as violation_events,
        COUNT(CASE WHEN severity = 'CRITICAL' THEN 1 END) as critical_events,
        COUNT(CASE WHEN severity = 'HIGH' THEN 1 END) as high_severity_events,
        COUNT(CASE WHEN is_processed = false THEN 1 END) as unprocessed_events,
        AVG(confidence_score) as avg_confidence_score
    FROM geofence_events 
    WHERE geofence_id = geofence_id_param
    AND occurred_at BETWEEN start_date AND end_date;
END;
$$ LANGUAGE plpgsql;

-- Function to get geofence alert statistics
CREATE OR REPLACE FUNCTION get_geofence_alert_statistics(
    start_date TIMESTAMP,
    end_date TIMESTAMP
) RETURNS TABLE(
    total_alerts BIGINT,
    active_alerts BIGINT,
    acknowledged_alerts BIGINT,
    resolved_alerts BIGINT,
    critical_alerts BIGINT,
    high_severity_alerts BIGINT,
    unacknowledged_alerts BIGINT,
    avg_resolution_hours DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_alerts,
        COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_alerts,
        COUNT(CASE WHEN status = 'ACKNOWLEDGED' THEN 1 END) as acknowledged_alerts,
        COUNT(CASE WHEN status = 'RESOLVED' THEN 1 END) as resolved_alerts,
        COUNT(CASE WHEN severity = 'CRITICAL' THEN 1 END) as critical_alerts,
        COUNT(CASE WHEN severity = 'HIGH' THEN 1 END) as high_severity_alerts,
        COUNT(CASE WHEN acknowledged_at IS NULL THEN 1 END) as unacknowledged_alerts,
        AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as avg_resolution_hours
    FROM geofence_alerts 
    WHERE created_at BETWEEN start_date AND end_date;
END;
$$ LANGUAGE plpgsql;

-- Create views for common geofencing queries

-- View for geofence summary
CREATE VIEW geofence_summary AS
SELECT 
    g.id,
    g.name,
    g.geofence_type,
    g.priority,
    g.is_active,
    COUNT(ge.id) as event_count,
    COUNT(ga.id) as alert_count,
    COUNT(CASE WHEN ga.status = 'ACTIVE' THEN 1 END) as active_alert_count,
    g.last_checked_at,
    g.last_alert_at
FROM geofences g
LEFT JOIN geofence_events ge ON g.id = ge.geofence_id
LEFT JOIN geofence_alerts ga ON g.id = ga.geofence_id
GROUP BY g.id, g.name, g.geofence_type, g.priority, g.is_active, g.last_checked_at, g.last_alert_at;

-- View for active geofence events
CREATE VIEW active_geofence_events AS
SELECT 
    ge.id,
    ge.geofence_id,
    g.name as geofence_name,
    ge.event_type,
    ST_X(ge.location) as longitude,
    ST_Y(ge.location) as latitude,
    ge.entity_type,
    ge.entity_name,
    ge.severity,
    ge.confidence_score,
    ge.occurred_at,
    ge.is_processed
FROM geofence_events ge
JOIN geofences g ON ge.geofence_id = g.id
WHERE g.is_active = true
AND ge.occurred_at >= CURRENT_TIMESTAMP - INTERVAL '24 hours';

-- View for active geofence alerts
CREATE VIEW active_geofence_alerts AS
SELECT 
    ga.id,
    ga.geofence_id,
    g.name as geofence_name,
    ga.alert_type,
    ga.title,
    ga.message,
    ga.severity,
    ga.status,
    ga.assigned_to,
    ga.created_at,
    ga.acknowledged_at,
    ga.resolved_at
FROM geofence_alerts ga
JOIN geofences g ON ga.geofence_id = g.id
WHERE ga.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')
ORDER BY ga.created_at DESC;



