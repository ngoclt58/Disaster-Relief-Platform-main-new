-- Inventory and tasks domain enhancements

-- Create inventory hubs table (if not exists)
CREATE TABLE IF NOT EXISTS inventory_hubs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address TEXT,
    geom_point geometry(Point, 4326),
    capacity INTEGER,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create inventory stock table (if not exists)
CREATE TABLE IF NOT EXISTS inventory_stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_id UUID REFERENCES inventory_hubs(id),
    item_id UUID REFERENCES items_catalog(id),
    qty_available INTEGER DEFAULT 0,
    qty_reserved INTEGER DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(hub_id, item_id)
);

-- Create tasks table (if not exists)
CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES needs_requests(id),
    assignee_id UUID REFERENCES users(id),
    hub_id UUID REFERENCES inventory_hubs(id),
    planned_kit_code VARCHAR(100),
    status VARCHAR(50) DEFAULT 'new',
    eta TIMESTAMPTZ,
    route_id UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Create deliveries table (if not exists)
CREATE TABLE IF NOT EXISTS deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID REFERENCES tasks(id),
    delivered_at TIMESTAMPTZ,
    proof_media_id UUID,
    notes TEXT,
    recipient_name VARCHAR(255),
    recipient_phone VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create media table (if not exists)
CREATE TABLE IF NOT EXISTS media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    url TEXT NOT NULL,
    sha256 VARCHAR(64),
    taken_at TIMESTAMPTZ,
    geom_point geometry(Point, 4326),
    redacted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create status history table (if not exists)
CREATE TABLE IF NOT EXISTS status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    by_user_id UUID REFERENCES users(id),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create geofences table (if not exists)
CREATE TABLE IF NOT EXISTS geofences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    area_geom geometry(Polygon, 4326),
    active BOOLEAN DEFAULT TRUE,
    province_id UUID REFERENCES provinces(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create dedupe groups table (if not exists)
CREATE TABLE IF NOT EXISTS dedupe_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reason TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create dedupe links table (if not exists)
CREATE TABLE IF NOT EXISTS dedupe_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID REFERENCES dedupe_groups(id),
    request_id UUID REFERENCES needs_requests(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create notifications table (if not exists)
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    message TEXT,
    payload_json JSONB,
    sent_at TIMESTAMPTZ,
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create audit logs table (if not exists)
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Add indexes for inventory and tasks
CREATE INDEX IF NOT EXISTS idx_inventory_hubs_geom ON inventory_hubs USING GIST (geom_point);
CREATE INDEX IF NOT EXISTS idx_inventory_stock_hub_item ON inventory_stock (hub_id, item_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assignee ON tasks (assignee_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_tasks_request ON tasks (request_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_task ON deliveries (task_id);
CREATE INDEX IF NOT EXISTS idx_media_owner ON media (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_media_geom ON media USING GIST (geom_point);
CREATE INDEX IF NOT EXISTS idx_status_history_entity ON status_history (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_geofences_geom ON geofences USING GIST (area_geom);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON audit_logs (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs (entity_type, entity_id);

-- Add constraints for task status transitions
ALTER TABLE tasks ADD CONSTRAINT check_task_status 
    CHECK (status IN ('new', 'assigned', 'picked_up', 'delivered', 'could_not_deliver', 'cancelled'));

-- Add constraints for delivery status
ALTER TABLE deliveries ADD CONSTRAINT check_delivery_required 
    CHECK (delivered_at IS NOT NULL OR proof_media_id IS NOT NULL);

-- Add constraints for media types
ALTER TABLE media ADD CONSTRAINT check_media_type 
    CHECK (type IN ('image', 'video', 'document', 'audio'));

-- Add constraints for notification status
ALTER TABLE notifications ADD CONSTRAINT check_notification_status 
    CHECK (status IN ('pending', 'sent', 'failed', 'cancelled'));

-- Add constraints for audit log actions
ALTER TABLE audit_logs ADD CONSTRAINT check_audit_action 
    CHECK (action IN ('create', 'update', 'delete', 'assign', 'claim', 'deliver', 'cancel'));

-- Insert sample inventory hubs
INSERT INTO inventory_hubs (id, name, address, geom_point, capacity) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'Central Relief Hub', '123 Main Street, Central City', 
 ST_GeomFromText('POINT(0 0)', 4326), 1000),
('550e8400-e29b-41d4-a716-446655440002', 'North Distribution Center', '456 North Ave, North City', 
 ST_GeomFromText('POINT(0.1 0.1)', 4326), 500),
('550e8400-e29b-41d4-a716-446655440003', 'South Emergency Depot', '789 South St, South City', 
 ST_GeomFromText('POINT(-0.1 -0.1)', 4326), 750)
ON CONFLICT (id) DO NOTHING;

-- Insert initial stock for all hubs
INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved) 
SELECT 
    h.id as hub_id,
    i.id as item_id,
    CASE 
        WHEN h.name = 'Central Relief Hub' THEN 100
        WHEN h.name = 'North Distribution Center' THEN 50
        ELSE 75
    END as qty_available,
    0 as qty_reserved
FROM inventory_hubs h
CROSS JOIN items_catalog i
ON CONFLICT (hub_id, item_id) DO NOTHING;

-- Create function to log status changes
CREATE OR REPLACE FUNCTION log_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.status != NEW.status THEN
        INSERT INTO status_history (entity_type, entity_id, from_status, to_status, by_user_id, created_at)
        VALUES (TG_TABLE_NAME, NEW.id, OLD.status, NEW.status, NEW.assignee_id, NOW());
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for task status changes
DROP TRIGGER IF EXISTS task_status_change_trigger ON tasks;
CREATE TRIGGER task_status_change_trigger
    AFTER UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION log_status_change();

-- Create trigger for needs request status changes
DROP TRIGGER IF EXISTS needs_status_change_trigger ON needs_requests;
CREATE TRIGGER needs_status_change_trigger
    AFTER UPDATE ON needs_requests
    FOR EACH ROW
    EXECUTE FUNCTION log_status_change();



