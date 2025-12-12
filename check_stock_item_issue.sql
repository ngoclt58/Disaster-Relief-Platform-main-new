-- SQL to check and fix stock entries with missing items

-- 1. Check stock entries and their relationships
SELECT 
    s.id AS stock_id,
    s.hub_id,
    s.item_id,
    h.name AS hub_name,
    i.name AS item_name,
    i.code AS item_code,
    s.qty_available,
    s.qty_reserved,
    CASE 
        WHEN s.hub_id IS NULL THEN 'MISSING HUB'
        WHEN s.item_id IS NULL THEN 'MISSING ITEM'
        WHEN h.id IS NULL THEN 'INVALID HUB ID'
        WHEN i.id IS NULL THEN 'INVALID ITEM ID'
        ELSE 'OK'
    END AS status
FROM inventory_stock s
LEFT JOIN inventory_hubs h ON s.hub_id = h.id
LEFT JOIN items_catalog i ON s.item_id = i.id
ORDER BY s.updated_at DESC;

-- 2. Find stock entries with NULL item_id (these won't show in UI)
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

-- 3. Find stock entries with invalid item_id (references non-existent item)
SELECT 
    s.id AS stock_id,
    s.hub_id,
    s.item_id,
    h.name AS hub_name,
    s.qty_available,
    s.qty_reserved
FROM inventory_stock s
LEFT JOIN inventory_hubs h ON s.hub_id = h.id
LEFT JOIN items_catalog i ON s.item_id = i.id
WHERE s.item_id IS NOT NULL AND i.id IS NULL;

-- 4. If you need to fix a stock entry, update it with a valid item_id
-- Replace the UUIDs with actual values from your database
-- First, get a list of available items:
SELECT id, code, name FROM items_catalog ORDER BY name;

-- Then update the stock entry (example):
-- UPDATE inventory_stock 
-- SET item_id = 'YOUR_ITEM_UUID_HERE'
-- WHERE id = 'YOUR_STOCK_UUID_HERE' AND item_id IS NULL;

