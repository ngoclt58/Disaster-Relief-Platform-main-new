-- Create PostGIS spatial indexes for better geospatial query performance

-- Provinces spatial index
CREATE INDEX provinces_geom_gist ON provinces USING GIST (geom);

-- Districts spatial index
CREATE INDEX districts_geom_gist ON districts USING GIST (geom);

-- Wards spatial index
CREATE INDEX wards_geom_gist ON wards USING GIST (geom);

-- Households spatial index
CREATE INDEX households_geom_gist ON households USING GIST (geom_point);

-- Incidents spatial index
CREATE INDEX incidents_area_geom_gist ON incidents USING GIST (area_geom);

-- Needs requests spatial index
CREATE INDEX needs_requests_geom_gist ON needs_requests USING GIST (geom_point);

-- Inventory hubs spatial index
CREATE INDEX inventory_hubs_geom_gist ON inventory_hubs USING GIST (geom_point);

-- Media spatial index
CREATE INDEX media_geom_gist ON media USING GIST (geom_point);

-- Note: Geofences spatial index is created in V7__Create_geofencing_tables.sql

-- Create additional indexes for common geospatial queries
CREATE INDEX needs_requests_created_at_geom ON needs_requests (created_at, geom_point);
CREATE INDEX households_ward_geom ON households (ward_id, geom_point);
CREATE INDEX tasks_status_created_at ON tasks (status, created_at);

-- Create partial indexes for active records
CREATE INDEX idx_needs_requests_active ON needs_requests (status, created_at) 
WHERE status IN ('new', 'assigned', 'in_progress');

CREATE INDEX idx_tasks_active ON tasks (status, created_at) 
WHERE status IN ('new', 'assigned', 'picked_up');

-- Create indexes for time-based queries
CREATE INDEX idx_needs_requests_created_at ON needs_requests (created_at DESC);
CREATE INDEX idx_tasks_created_at ON tasks (created_at DESC);
CREATE INDEX idx_deliveries_delivered_at ON deliveries (delivered_at DESC);
CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);

