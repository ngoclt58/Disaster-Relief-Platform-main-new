-- Create roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    org_id UUID,
    device_token TEXT,
    disabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Create provinces table
CREATE TABLE provinces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(10) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT FALSE,
    geom geometry(Polygon, 4326),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create districts table
CREATE TABLE districts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    province_id UUID REFERENCES provinces(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    geom geometry(Polygon, 4326),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create wards table
CREATE TABLE wards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    district_id UUID REFERENCES districts(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    geom geometry(Polygon, 4326),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create households table
CREATE TABLE households (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resident_user_id UUID REFERENCES users(id),
    address TEXT,
    ward_id UUID REFERENCES wards(id),
    geom_point geometry(Point, 4326),
    household_size INTEGER,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create incidents table
CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL, -- flood, hurricane, landslide, etc.
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ,
    status VARCHAR(50) DEFAULT 'active', -- active, resolved, cancelled
    area_geom geometry(Polygon, 4326),
    province_id UUID REFERENCES provinces(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create items catalog table
CREATE TABLE items_catalog (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    unit VARCHAR(50) NOT NULL, -- kg, liters, pieces, etc.
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create needs requests table
CREATE TABLE needs_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID REFERENCES households(id),
    type VARCHAR(50) NOT NULL, -- food, water, medical, evacuation, sos, other
    severity INTEGER CHECK (severity BETWEEN 1 AND 5),
    notes TEXT,
    source VARCHAR(50) DEFAULT 'app', -- app, sms, call, ussd
    status VARCHAR(50) DEFAULT 'new', -- new, assigned, in_progress, completed, cancelled
    geom_point geometry(Point, 4326),
    last_seen_at TIMESTAMPTZ,
    created_by UUID REFERENCES users(id),
    current_assignee_id UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Create needs items table (many-to-many between requests and items)
CREATE TABLE needs_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES needs_requests(id) ON DELETE CASCADE,
    item_id UUID REFERENCES items_catalog(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create inventory hubs table
CREATE TABLE inventory_hubs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address TEXT,
    geom_point geometry(Point, 4326),
    capacity INTEGER,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create inventory stock table
CREATE TABLE inventory_stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_id UUID REFERENCES inventory_hubs(id),
    item_id UUID REFERENCES items_catalog(id),
    qty_available INTEGER DEFAULT 0,
    qty_reserved INTEGER DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(hub_id, item_id)
);

-- Create tasks table
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES needs_requests(id),
    assignee_id UUID REFERENCES users(id),
    hub_id UUID REFERENCES inventory_hubs(id),
    planned_kit_code VARCHAR(100),
    status VARCHAR(50) DEFAULT 'new', -- new, assigned, picked_up, delivered, could_not_deliver
    eta TIMESTAMPTZ,
    route_id UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Create deliveries table
CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID REFERENCES tasks(id),
    delivered_at TIMESTAMPTZ,
    proof_media_id UUID,
    notes TEXT,
    recipient_name VARCHAR(255),
    recipient_phone VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create media table
CREATE TABLE media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID REFERENCES users(id),
    type VARCHAR(50) NOT NULL, -- image, video, document
    url TEXT NOT NULL,
    sha256 VARCHAR(64),
    taken_at TIMESTAMPTZ,
    geom_point geometry(Point, 4326),
    redacted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create status history table
CREATE TABLE status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL, -- needs_request, task, delivery, etc.
    entity_id UUID NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    by_user_id UUID REFERENCES users(id),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Note: geofences table is created in V7__Create_geofencing_tables.sql with full structure

-- Create dedupe groups table
CREATE TABLE dedupe_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reason TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create dedupe links table
CREATE TABLE dedupe_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID REFERENCES dedupe_groups(id),
    request_id UUID REFERENCES needs_requests(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    message TEXT,
    payload_json JSONB,
    sent_at TIMESTAMPTZ,
    status VARCHAR(50) DEFAULT 'pending', -- pending, sent, failed
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create audit logs table
CREATE TABLE audit_logs (
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

-- Add indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_households_resident ON households(resident_user_id);
CREATE INDEX idx_households_ward ON households(ward_id);
CREATE INDEX idx_needs_requests_household ON needs_requests(household_id);
CREATE INDEX idx_needs_requests_status ON needs_requests(status);
CREATE INDEX idx_needs_requests_type ON needs_requests(type);
CREATE INDEX idx_needs_requests_assignee ON needs_requests(current_assignee_id);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_deliveries_task ON deliveries(task_id);
CREATE INDEX idx_media_owner ON media(owner_user_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

