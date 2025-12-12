-- Quick fix for stock entry with missing item
-- This will help you fix the stock entry that has a hub but no item

-- Step 1: Check the current state
SELECT 
    s.id AS stock_id,
    s.hub_id,
    s.item_id,
    h.name AS hub_name,
    i.name AS item_name,
    s.qty_available,
    s.qty_reserved
FROM inventory_stock s
LEFT JOIN inventory_hubs h ON s.hub_id = h.id
LEFT JOIN items_catalog i ON s.item_id = i.id;

-- Step 2: See which stock entry has NULL item_id
SELECT 
    s.id AS stock_id,
    s.hub_id,
    h.name AS hub_name,
    s.qty_available,
    s.qty_reserved,
    s.updated_at
FROM inventory_stock s
LEFT JOIN inventory_hubs h ON s.hub_id = h.id
WHERE s.item_id IS NULL;

-- Step 3: Get available items to choose from
SELECT id, code, name, unit 
FROM items_catalog 
ORDER BY name
LIMIT 10;

-- Step 4: Fix the stock entry by setting a valid item_id
-- Replace 'ITEM_UUID_HERE' with one of the item IDs from Step 3
-- Replace 'STOCK_UUID_HERE' with the stock_id from Step 2
UPDATE inventory_stock 
SET item_id = (
    SELECT id FROM items_catalog 
    WHERE code = 'WATER-001' OR code = 'FOOD-001' OR code = 'BLANKET-001'
    LIMIT 1
)
WHERE item_id IS NULL;

-- Step 5: Verify the fix
SELECT 
    s.id AS stock_id,
    h.name AS hub_name,
    i.name AS item_name,
    i.code AS item_code,
    s.qty_available,
    s.qty_reserved
FROM inventory_stock s
LEFT JOIN inventory_hubs h ON s.hub_id = h.id
LEFT JOIN items_catalog i ON s.item_id = i.id
WHERE s.item_id IS NOT NULL;

