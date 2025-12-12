-- Seed data for admin user and basic roles

-- Insert basic roles
INSERT INTO roles (code, description) VALUES 
('ADMIN', 'System Administrator with full access'),
('DISPATCHER', 'Dispatcher/Coordinator role for managing requests and tasks'),
('HELPER', 'Helper/Volunteer role for accepting and completing tasks'),
('RESIDENT', 'Resident role for reporting needs and status'),
('PARTNER', 'Partner/NGO role for resource sharing')
ON CONFLICT (code) DO NOTHING;

-- Insert admin user (password: admin123)
-- Password hash is for 'admin123' using BCrypt (strength 10)
-- Verified hash that works with BCryptPasswordEncoder
INSERT INTO users (id, full_name, email, password_hash, role, disabled, created_at, updated_at) VALUES 
('550e8400-e29b-41d4-a716-446655440000', 'System Administrator', 'admin@relief.local', 
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', false, NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET
  password_hash = EXCLUDED.password_hash,
  disabled = false,
  updated_at = NOW();

-- Insert sample items catalog
INSERT INTO items_catalog (code, name, unit) VALUES 
('FOOD_BASIC', 'Basic Food Kit', 'kit'),
('WATER_BOTTLES', 'Water Bottles', 'liters'),
('HYGIENE_KIT', 'Hygiene Kit', 'kit'),
('BLANKET', 'Emergency Blanket', 'piece'),
('MEDICAL_FIRST_AID', 'First Aid Kit', 'kit'),
('BATTERY_PACK', 'Portable Battery Pack', 'piece'),
('FLASHLIGHT', 'Emergency Flashlight', 'piece'),
('RADIO', 'Emergency Radio', 'piece'),
('TARP', 'Emergency Tarp', 'piece'),
('ROPE', 'Emergency Rope', 'meters')
ON CONFLICT (code) DO NOTHING;

-- Insert sample inventory hub
INSERT INTO inventory_hubs (id, name, address, geom_point, capacity) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'Central Relief Hub', '123 Main Street, Central City', 
 ST_GeomFromText('POINT(0 0)', 4326), 1000)
ON CONFLICT (id) DO NOTHING;

-- Insert initial stock for the hub
INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved) 
SELECT 
    '550e8400-e29b-41d4-a716-446655440001' as hub_id,
    id as item_id,
    100 as qty_available,
    0 as qty_reserved
FROM items_catalog
ON CONFLICT (hub_id, item_id) DO NOTHING;

-- Insert sample provinces and wards for testing
INSERT INTO provinces (id, name, code, geom) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'Sample Province', 'PROV001', 
 ST_GeomFromText('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))', 4326)),
('550e8400-e29b-41d4-a716-446655440011', 'Sample Province 2', 'PROV002', 
 ST_GeomFromText('POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))', 4326))
ON CONFLICT (code) DO NOTHING;

-- Insert sample districts
-- Note: districts.code is not unique, so we use ON CONFLICT (id) instead
INSERT INTO districts (id, name, code, province_id, geom) VALUES
('550e8400-e29b-41d4-a716-446655440020', 'Sample District', 'DIST001', 
 '550e8400-e29b-41d4-a716-446655440010',
 ST_GeomFromText('POLYGON((0.2 0.2, 0.8 0.2, 0.8 0.8, 0.2 0.8, 0.2 0.2))', 4326))
ON CONFLICT (id) DO NOTHING;

-- Insert sample wards
-- Note: wards.code is not unique, so we use ON CONFLICT (id) instead
INSERT INTO wards (id, name, code, district_id, geom) VALUES
('550e8400-e29b-41d4-a716-446655440030', 'Sample Ward', 'WARD001', 
 '550e8400-e29b-41d4-a716-446655440020',
 ST_GeomFromText('POLYGON((0.3 0.3, 0.7 0.3, 0.7 0.7, 0.3 0.7, 0.3 0.3))', 4326))
ON CONFLICT (id) DO NOTHING;

