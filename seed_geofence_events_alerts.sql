-- SQL script to seed Geofence Events and Alerts
-- This script will use the first active geofence in your database
-- If you don't have any geofences, create one first using the Geofencing Dashboard

-- Step 1: Get the first geofence ID (or create one if none exists)
-- First, let's check and create a geofence if needed
DO $$
DECLARE
    geofence_count INTEGER;
    sample_geofence_id BIGINT;
BEGIN
    SELECT COUNT(*) INTO geofence_count FROM geofences;
    
    IF geofence_count = 0 THEN
        INSERT INTO geofences (
            name, description, boundary, geofence_type, priority, is_active,
            buffer_distance_meters, check_interval_seconds, alert_threshold,
            cooldown_period_seconds, notification_channels, auto_actions,
            created_by
        ) VALUES (
            'Sample Disaster Zone',
            'Sample geofence for testing events and alerts',
            ST_SetSRID(ST_MakePolygon(ST_MakeLine(ARRAY[
                ST_MakePoint(-74.0, 40.7),
                ST_MakePoint(-73.8, 40.7),
                ST_MakePoint(-73.8, 40.9),
                ST_MakePoint(-74.0, 40.9),
                ST_MakePoint(-74.0, 40.7)
            ])), 4326),
            'DISASTER_ZONE',
            'CRITICAL',
            true,
            500.0,
            60,
            1,
            1800,
            '["email", "sms", "push"]'::jsonb,
            '["notify_emergency_team"]'::jsonb,
            'system_admin'
        ) RETURNING id INTO sample_geofence_id;
    END IF;
END $$;

-- Step 2: Insert Geofence Events
-- Using the first active geofence
INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'ENTRY',
    ST_SetSRID(ST_MakePoint(-74.0, 40.7), 4326),
    'PERSON',
    101,
    'John Doe',
    '{"speed": 5.2, "direction": "north"}'::jsonb,
    'HIGH',
    0.95,
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '2 hours',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'EXIT',
    ST_SetSRID(ST_MakePoint(-73.9, 40.75), 4326),
    'VEHICLE',
    102,
    'Ambulance #123',
    '{"speed": 45.0, "license_plate": "AMB-123"}'::jsonb,
    'MEDIUM',
    0.88,
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'VIOLATION',
    ST_SetSRID(ST_MakePoint(-74.05, 40.65), 4326),
    'VEHICLE',
    103,
    'Unauthorized Vehicle',
    '{"speed": 60.0, "violation_type": "unauthorized_access"}'::jsonb,
    'CRITICAL',
    0.98,
    NOW() - INTERVAL '30 minutes',
    NOW() - INTERVAL '30 minutes',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'EMERGENCY',
    ST_SetSRID(ST_MakePoint(-73.95, 40.72), 4326),
    'PERSON',
    104,
    'Emergency Responder',
    '{"emergency_type": "medical", "priority": "urgent"}'::jsonb,
    'CRITICAL',
    0.99,
    NOW() - INTERVAL '15 minutes',
    NOW() - INTERVAL '15 minutes',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'DWELL',
    ST_SetSRID(ST_MakePoint(-74.02, 40.68), 4326),
    'VEHICLE',
    105,
    'Supply Truck #456',
    '{"dwell_time_minutes": 45, "purpose": "delivery"}'::jsonb,
    'LOW',
    0.85,
    NOW() - INTERVAL '45 minutes',
    NOW() - INTERVAL '45 minutes',
    true
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'PROXIMITY',
    ST_SetSRID(ST_MakePoint(-73.88, 40.78), 4326),
    'PERSON',
    106,
    'Resident #789',
    '{"distance_meters": 150, "approaching": true}'::jsonb,
    'MEDIUM',
    0.90,
    NOW() - INTERVAL '20 minutes',
    NOW() - INTERVAL '20 minutes',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'RESOURCE_DEPLETION',
    ST_SetSRID(ST_MakePoint(-74.1, 40.7), 4326),
    'FACILITY',
    107,
    'Resource Depot Alpha',
    '{"resource_type": "water", "remaining_percent": 15}'::jsonb,
    'HIGH',
    0.92,
    NOW() - INTERVAL '1 hour 30 minutes',
    NOW() - INTERVAL '1 hour 30 minutes',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'CAPACITY_EXCEEDED',
    ST_SetSRID(ST_MakePoint(-73.92, 40.74), 4326),
    'FACILITY',
    108,
    'Emergency Shelter Beta',
    '{"capacity": 200, "current": 215, "overflow": 15}'::jsonb,
    'HIGH',
    0.94,
    NOW() - INTERVAL '3 hours',
    NOW() - INTERVAL '3 hours',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'STATUS_CHANGE',
    ST_SetSRID(ST_MakePoint(-74.08, 40.66), 4326),
    'FACILITY',
    109,
    'Medical Facility Gamma',
    '{"old_status": "operational", "new_status": "overcrowded"}'::jsonb,
    'MEDIUM',
    0.87,
    NOW() - INTERVAL '4 hours',
    NOW() - INTERVAL '4 hours',
    true
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'ENTRY',
    ST_SetSRID(ST_MakePoint(-73.97, 40.71), 4326),
    'PERSON',
    110,
    'Jane Smith',
    '{"speed": 3.5, "direction": "east"}'::jsonb,
    'LOW',
    0.82,
    NOW() - INTERVAL '5 minutes',
    NOW() - INTERVAL '5 minutes',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'VIOLATION',
    ST_SetSRID(ST_MakePoint(-74.03, 40.69), 4326),
    'VEHICLE',
    111,
    'Suspicious Vehicle',
    '{"speed": 70.0, "violation_type": "speed_limit"}'::jsonb,
    'CRITICAL',
    0.96,
    NOW() - INTERVAL '10 minutes',
    NOW() - INTERVAL '10 minutes',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_events (
    geofence_id, event_type, location, entity_type, entity_id, entity_name,
    event_data, severity, confidence_score, occurred_at, detected_at, is_processed
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'EMERGENCY',
    ST_SetSRID(ST_MakePoint(-73.89, 40.76), 4326),
    'PERSON',
    112,
    'Injured Person',
    '{"emergency_type": "medical", "severity": "critical"}'::jsonb,
    'CRITICAL',
    0.97,
    NOW() - INTERVAL '8 minutes',
    NOW() - INTERVAL '8 minutes',
    false
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

-- Step 3: Insert Geofence Alerts
-- Get the first event ID to link alerts to events
INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'BOUNDARY_VIOLATION',
    'Unauthorized Entry Detected',
    'A vehicle has entered the restricted disaster zone without authorization. Immediate action required.',
    'CRITICAL',
    'ACTIVE',
    (SELECT id FROM geofence_events WHERE event_type = 'VIOLATION' ORDER BY occurred_at DESC LIMIT 1),
    '{"violation_type": "unauthorized_entry", "vehicle_id": "ABC-123"}'::jsonb,
    '["email", "sms", "push"]'::jsonb,
    '["notify_security_team", "log_violation"]'::jsonb,
    NULL,
    NOW() - INTERVAL '30 minutes'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'CAPACITY_EXCEEDED',
    'Shelter Overcapacity',
    'Emergency shelter has exceeded its maximum capacity. Additional resources needed.',
    'HIGH',
    'ACTIVE',
    (SELECT id FROM geofence_events WHERE event_type = 'CAPACITY_EXCEEDED' ORDER BY occurred_at DESC LIMIT 1),
    '{"facility": "Shelter Beta", "capacity": 200, "current": 215}'::jsonb,
    '["email", "sms"]'::jsonb,
    '["notify_shelter_management", "request_resources"]'::jsonb,
    'shelter_manager',
    NOW() - INTERVAL '3 hours'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'EMERGENCY_DETECTED',
    'Medical Emergency Reported',
    'A medical emergency has been detected within the geofence. Medical team dispatched.',
    'CRITICAL',
    'ACTIVE',
    (SELECT id FROM geofence_events WHERE event_type = 'EMERGENCY' ORDER BY occurred_at DESC LIMIT 1),
    '{"emergency_type": "medical", "location": "lat:40.76, lon:-73.89"}'::jsonb,
    '["email", "sms", "push"]'::jsonb,
    '["dispatch_medical_team", "update_status"]'::jsonb,
    'medical_team_lead',
    NOW() - INTERVAL '8 minutes'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'RESOURCE_DEPLETION',
    'Water Supply Low',
    'Water supply at Resource Depot Alpha has dropped below 20%. Replenishment needed.',
    'HIGH',
    'ACKNOWLEDGED',
    (SELECT id FROM geofence_events WHERE event_type = 'RESOURCE_DEPLETION' ORDER BY occurred_at DESC LIMIT 1),
    '{"resource_type": "water", "remaining_percent": 15, "depot": "Alpha"}'::jsonb,
    '["email"]'::jsonb,
    '["notify_logistics", "order_supplies"]'::jsonb,
    'logistics_coordinator',
    NOW() - INTERVAL '1 hour 30 minutes'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'THRESHOLD_EXCEEDED',
    'Multiple Violations Detected',
    'Multiple boundary violations detected in the past hour. Security review required.',
    'HIGH',
    'IN_PROGRESS',
    NULL,
    '{"violation_count": 3, "time_window": "1 hour"}'::jsonb,
    '["email", "sms"]'::jsonb,
    '["notify_security_team", "escalate_to_manager"]'::jsonb,
    'security_officer',
    NOW() - INTERVAL '45 minutes'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'UNAUTHORIZED_ACCESS',
    'Unauthorized Person Entry',
    'An unauthorized person entered the restricted zone. Situation resolved.',
    'MEDIUM',
    'RESOLVED',
    (SELECT id FROM geofence_events WHERE event_type = 'ENTRY' ORDER BY occurred_at DESC LIMIT 1),
    '{"person_id": 101, "resolution": "escorted_out"}'::jsonb,
    '["email"]'::jsonb,
    '["log_incident"]'::jsonb,
    'security_officer',
    NOW() - INTERVAL '2 hours'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'MAINTENANCE_REQUIRED',
    'Facility Status Change',
    'Medical Facility Gamma status changed to overcrowded. Additional staff may be needed.',
    'MEDIUM',
    'ACTIVE',
    (SELECT id FROM geofence_events WHERE event_type = 'STATUS_CHANGE' ORDER BY occurred_at DESC LIMIT 1),
    '{"facility": "Medical Facility Gamma", "old_status": "operational", "new_status": "overcrowded"}'::jsonb,
    '["email"]'::jsonb,
    '["notify_facility_manager"]'::jsonb,
    NULL,
    NOW() - INTERVAL '4 hours'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'EMERGENCY_DETECTED',
    'Critical Medical Emergency',
    'Critical medical emergency requires immediate attention. All available resources mobilized.',
    'CRITICAL',
    'ESCALATED',
    (SELECT id FROM geofence_events WHERE event_type = 'EMERGENCY' ORDER BY occurred_at DESC LIMIT 1 OFFSET 1),
    '{"emergency_type": "medical", "severity": "critical", "patient_count": 1}'::jsonb,
    '["email", "sms", "push"]'::jsonb,
    '["dispatch_all_medical_teams", "alert_hospital"]'::jsonb,
    'emergency_coordinator',
    NOW() - INTERVAL '12 minutes'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'TIME_BASED_ALERT',
    'Routine Check Required',
    'Scheduled routine check for geofence monitoring. All systems operational.',
    'LOW',
    'ACTIVE',
    NULL,
    '{"check_type": "routine", "interval": "hourly"}'::jsonb,
    '["email"]'::jsonb,
    '[]'::jsonb,
    NULL,
    NOW() - INTERVAL '1 hour'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

INSERT INTO geofence_alerts (
    geofence_id, alert_type, title, message, severity, status,
    triggered_by_event_id, alert_data, notification_channels, auto_actions_triggered,
    assigned_to, created_at
)
SELECT 
    (SELECT id FROM geofences WHERE is_active = true LIMIT 1),
    'BOUNDARY_VIOLATION',
    'Speed Violation Detected',
    'Vehicle exceeded speed limit within geofence. Warning issued.',
    'MEDIUM',
    'ACKNOWLEDGED',
    (SELECT id FROM geofence_events WHERE event_type = 'VIOLATION' ORDER BY occurred_at DESC LIMIT 1 OFFSET 1),
    '{"violation_type": "speed", "speed": 70, "limit": 50}'::jsonb,
    '["email"]'::jsonb,
    '["log_violation", "issue_warning"]'::jsonb,
    'traffic_officer',
    NOW() - INTERVAL '10 minutes'
WHERE EXISTS (SELECT 1 FROM geofences WHERE is_active = true);

-- Step 4: Update alerts with acknowledgment and resolution timestamps
UPDATE geofence_alerts
SET 
    acknowledged_at = created_at + INTERVAL '5 minutes',
    acknowledged_by = assigned_to
WHERE status IN ('ACKNOWLEDGED', 'IN_PROGRESS', 'RESOLVED')
AND acknowledged_at IS NULL
AND assigned_to IS NOT NULL;

UPDATE geofence_alerts
SET 
    resolved_at = created_at + INTERVAL '1 hour',
    resolved_by = assigned_to,
    resolution_notes = 'Issue resolved successfully. All systems back to normal.'
WHERE status = 'RESOLVED'
AND resolved_at IS NULL;

UPDATE geofence_alerts
SET 
    escalated_at = created_at + INTERVAL '10 minutes',
    escalated_to = 'senior_coordinator',
    escalation_reason = 'Critical emergency requiring senior management attention.'
WHERE status = 'ESCALATED'
AND escalated_at IS NULL;

-- Step 5: Display summary
SELECT 
    'Geofence Events' as table_name,
    COUNT(*) as record_count
FROM geofence_events
UNION ALL
SELECT 
    'Geofence Alerts' as table_name,
    COUNT(*) as record_count
FROM geofence_alerts;

-- Show recent events
SELECT 
    'Recent Events' as info,
    ge.id,
    g.name as geofence_name,
    ge.event_type,
    ge.entity_name,
    ge.severity,
    ge.occurred_at
FROM geofence_events ge
JOIN geofences g ON ge.geofence_id = g.id
ORDER BY ge.occurred_at DESC
LIMIT 5;

-- Show active alerts
SELECT 
    'Active Alerts' as info,
    ga.id,
    g.name as geofence_name,
    ga.alert_type,
    ga.title,
    ga.severity,
    ga.status,
    ga.created_at
FROM geofence_alerts ga
JOIN geofences g ON ga.geofence_id = g.id
WHERE ga.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS', 'ESCALATED')
ORDER BY ga.created_at DESC
LIMIT 5;
