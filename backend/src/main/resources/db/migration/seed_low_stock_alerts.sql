-- SQL script to seed low stock alerts for testing
-- This will create sample hubs, items, and stock entries with various alert levels

-- Step 1: Create sample inventory hubs (if they don't exist)
INSERT INTO inventory_hubs (id, name, address, capacity, created_at)
SELECT 
    gen_random_uuid(),
    'Main Distribution Center',
    '123 Relief Avenue, Disaster City, DC 12345',
    10000,
    now()
WHERE NOT EXISTS (SELECT 1 FROM inventory_hubs WHERE name = 'Main Distribution Center');

INSERT INTO inventory_hubs (id, name, address, capacity, created_at)
SELECT 
    gen_random_uuid(),
    'North Emergency Hub',
    '456 Emergency Road, North District, ND 67890',
    5000,
    now()
WHERE NOT EXISTS (SELECT 1 FROM inventory_hubs WHERE name = 'North Emergency Hub');

INSERT INTO inventory_hubs (id, name, address, capacity, created_at)
SELECT 
    gen_random_uuid(),
    'South Relief Station',
    '789 Relief Street, South Zone, SZ 11111',
    3000,
    now()
WHERE NOT EXISTS (SELECT 1 FROM inventory_hubs WHERE name = 'South Relief Station');

-- Step 2: Create sample items catalog (if they don't exist)
INSERT INTO items_catalog (id, code, name, unit, created_at)
SELECT 
    gen_random_uuid(),
    'WATER-001',
    'Bottled Water',
    'liters',
    now()
WHERE NOT EXISTS (SELECT 1 FROM items_catalog WHERE code = 'WATER-001');

INSERT INTO items_catalog (id, code, name, unit, created_at)
SELECT 
    gen_random_uuid(),
    'FOOD-001',
    'Emergency Food Rations',
    'kg',
    now()
WHERE NOT EXISTS (SELECT 1 FROM items_catalog WHERE code = 'FOOD-001');

INSERT INTO items_catalog (id, code, name, unit, created_at)
SELECT 
    gen_random_uuid(),
    'MED-001',
    'First Aid Kits',
    'pieces',
    now()
WHERE NOT EXISTS (SELECT 1 FROM items_catalog WHERE code = 'MED-001');

INSERT INTO items_catalog (id, code, name, unit, created_at)
SELECT 
    gen_random_uuid(),
    'BLANKET-001',
    'Emergency Blankets',
    'pieces',
    now()
WHERE NOT EXISTS (SELECT 1 FROM items_catalog WHERE code = 'BLANKET-001');

INSERT INTO items_catalog (id, code, name, unit, created_at)
SELECT 
    gen_random_uuid(),
    'TENT-001',
    'Emergency Tents',
    'pieces',
    now()
WHERE NOT EXISTS (SELECT 1 FROM items_catalog WHERE code = 'TENT-001');

INSERT INTO items_catalog (id, code, name, unit, created_at)
SELECT 
    gen_random_uuid(),
    'BATTERY-001',
    'Flashlight Batteries',
    'pieces',
    now()
WHERE NOT EXISTS (SELECT 1 FROM items_catalog WHERE code = 'BATTERY-001');

-- Step 3: Create stock entries with various alert levels
-- OUT OF STOCK (qty_available = 0)
INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved, updated_at)
SELECT 
    h.id,
    i.id,
    0,  -- OUT OF STOCK
    0,
    now()
FROM inventory_hubs h
CROSS JOIN items_catalog i
WHERE h.name = 'Main Distribution Center' 
  AND i.code = 'WATER-001'
  AND NOT EXISTS (
    SELECT 1 FROM inventory_stock 
    WHERE hub_id = h.id AND item_id = i.id
  );

-- CRITICAL STOCK (qty_available < 5)
INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved, updated_at)
SELECT 
    h.id,
    i.id,
    3,  -- CRITICAL (less than 5)
    1,
    now()
FROM inventory_hubs h
CROSS JOIN items_catalog i
WHERE h.name = 'Main Distribution Center' 
  AND i.code = 'FOOD-001'
  AND NOT EXISTS (
    SELECT 1 FROM inventory_stock 
    WHERE hub_id = h.id AND item_id = i.id
  );

INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved, updated_at)
SELECT 
    h.id,
    i.id,
    2,  -- CRITICAL (less than 5)
    0,
    now()
FROM inventory_hubs h
CROSS JOIN items_catalog i
WHERE h.name = 'North Emergency Hub' 
  AND i.code = 'MED-001'
  AND NOT EXISTS (
    SELECT 1 FROM inventory_stock 
    WHERE hub_id = h.id AND item_id = i.id
  );

-- LOW STOCK (qty_available < 25 but >= 5)
INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved, updated_at)
SELECT 
    h.id,
    i.id,
    15,  -- LOW STOCK (less than 25)
    5,
    now()
FROM inventory_hubs h
CROSS JOIN items_catalog i
WHERE h.name = 'Main Distribution Center' 
  AND i.code = 'BLANKET-001'
  AND NOT EXISTS (
    SELECT 1 FROM inventory_stock 
    WHERE hub_id = h.id AND item_id = i.id
  );

INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved, updated_at)
SELECT 
    h.id,
    i.id,
    10,  -- LOW STOCK
    2,
    now()
FROM inventory_hubs h
CROSS JOIN items_catalog i
WHERE h.name = 'North Emergency Hub' 
  AND i.code = 'TENT-001'
  AND NOT EXISTS (
    SELECT 1 FROM inventory_stock 
    WHERE hub_id = h.id AND item_id = i.id
  );

INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved, updated_at)
SELECT 
    h.id,
    i.id,
    8,  -- LOW STOCK
    0,
    now()
FROM inventory_hubs h
CROSS JOIN items_catalog i
WHERE h.name = 'South Relief Station' 
  AND i.code = 'BATTERY-001'
  AND NOT EXISTS (
    SELECT 1 FROM inventory_stock 
    WHERE hub_id = h.id AND item_id = i.id
  );

-- Add one more CRITICAL alert
INSERT INTO inventory_stock (hub_id, item_id, qty_available, qty_reserved, updated_at)
SELECT 
    h.id,
    i.id,
    4,  -- CRITICAL (less than 5)
    1,
    now()
FROM inventory_hubs h
CROSS JOIN items_catalog i
WHERE h.name = 'South Relief Station' 
  AND i.code = 'FOOD-001'
  AND NOT EXISTS (
    SELECT 1 FROM inventory_stock 
    WHERE hub_id = h.id AND item_id = i.id
  );

-- Summary: This will create:
-- 1 OUT OF STOCK alert (Water at Main Distribution Center)
-- 3 CRITICAL alerts (Food at Main DC, Medical at North Hub, Food at South Station)
-- 3 LOW STOCK alerts (Blankets at Main DC, Tents at North Hub, Batteries at South Station)
-- Total: 7 alerts should appear in the Stock Alerts component

