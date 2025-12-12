-- SQL Queries to Check Inventory Stock and Movements
-- Run these queries to verify stock additions and see where the data is stored

-- ============================================
-- 1. VIEW ALL INVENTORY STOCK (with Hub and Item details)
-- ============================================
SELECT 
    s.id AS stock_id,
    h.name AS hub_name,
    h.address AS hub_address,
    i.code AS item_code,
    i.name AS item_name,
    i.unit AS item_unit,
    s.qty_available,
    s.qty_reserved,
    (s.qty_available + s.qty_reserved) AS total_stock,
    s.updated_at AS last_updated
FROM inventory_stock s
LEFT JOIN inventory_hubs h ON s.hub_id = h.id
LEFT JOIN items_catalog i ON s.item_id = i.id
ORDER BY s.updated_at DESC;

-- ============================================
-- 2. VIEW RECENT STOCK MOVEMENTS (to see your addition)
-- ============================================
SELECT 
    sm.id AS movement_id,
    sm.movement_type,
    sm.quantity,
    sm.reason,
    h.name AS hub_name,
    i.code AS item_code,
    i.name AS item_name,
    u.email AS user_email,
    u.full_name AS user_name,
    sm.created_at AS movement_date
FROM stock_movements sm
LEFT JOIN inventory_hubs h ON sm.hub_id = h.id
LEFT JOIN items_catalog i ON sm.item_id = i.id
LEFT JOIN users u ON sm.user_id = u.id
ORDER BY sm.created_at DESC
LIMIT 20;

-- ============================================
-- 3. VIEW STOCK FOR A SPECIFIC HUB
-- ============================================
-- Replace 'Central Relief Hub' with your hub name
SELECT 
    h.name AS hub_name,
    i.code AS item_code,
    i.name AS item_name,
    s.qty_available,
    s.qty_reserved,
    s.updated_at
FROM inventory_stock s
JOIN inventory_hubs h ON s.hub_id = h.id
JOIN items_catalog i ON s.item_id = i.id
WHERE h.name = 'Central Relief Hub'
ORDER BY i.name;

-- ============================================
-- 4. VIEW STOCK FOR A SPECIFIC ITEM
-- ============================================
-- Replace 'WATER-001' with your item code
SELECT 
    h.name AS hub_name,
    i.code AS item_code,
    i.name AS item_name,
    s.qty_available,
    s.qty_reserved,
    s.updated_at
FROM inventory_stock s
JOIN inventory_hubs h ON s.hub_id = h.id
JOIN items_catalog i ON s.item_id = i.id
WHERE i.code = 'WATER-001'
ORDER BY h.name;

-- ============================================
-- 5. VIEW TODAY'S STOCK MOVEMENTS (your recent addition)
-- ============================================
SELECT 
    sm.movement_type,
    sm.quantity,
    sm.reason,
    h.name AS hub_name,
    i.name AS item_name,
    sm.created_at
FROM stock_movements sm
LEFT JOIN inventory_hubs h ON sm.hub_id = h.id
LEFT JOIN items_catalog i ON sm.item_id = i.id
WHERE DATE(sm.created_at) = CURRENT_DATE
ORDER BY sm.created_at DESC;

-- ============================================
-- 6. COUNT STOCK BY HUB
-- ============================================
SELECT 
    h.name AS hub_name,
    COUNT(*) AS total_items,
    SUM(s.qty_available) AS total_available,
    SUM(s.qty_reserved) AS total_reserved,
    SUM(s.qty_available + s.qty_reserved) AS total_stock
FROM inventory_stock s
JOIN inventory_hubs h ON s.hub_id = h.id
GROUP BY h.id, h.name
ORDER BY total_stock DESC;

-- ============================================
-- 7. COUNT STOCK BY ITEM
-- ============================================
SELECT 
    i.code AS item_code,
    i.name AS item_name,
    i.unit AS unit,
    COUNT(*) AS hub_count,
    SUM(s.qty_available) AS total_available,
    SUM(s.qty_reserved) AS total_reserved,
    SUM(s.qty_available + s.qty_reserved) AS total_stock
FROM inventory_stock s
JOIN items_catalog i ON s.item_id = i.id
GROUP BY i.id, i.code, i.name, i.unit
ORDER BY total_stock DESC;

-- ============================================
-- 8. VIEW LOW STOCK ITEMS (qty_available < 25)
-- ============================================
SELECT 
    h.name AS hub_name,
    i.code AS item_code,
    i.name AS item_name,
    s.qty_available,
    s.qty_reserved,
    CASE 
        WHEN s.qty_available = 0 THEN 'OUT OF STOCK'
        WHEN s.qty_available < 5 THEN 'CRITICAL'
        WHEN s.qty_available < 25 THEN 'LOW'
        ELSE 'OK'
    END AS stock_status
FROM inventory_stock s
JOIN inventory_hubs h ON s.hub_id = h.id
JOIN items_catalog i ON s.item_id = i.id
WHERE s.qty_available < 25
ORDER BY s.qty_available ASC;

-- ============================================
-- 9. VIEW STOCK MOVEMENTS FOR A SPECIFIC HUB AND ITEM
-- ============================================
-- Replace the hub and item names with your values
SELECT 
    sm.movement_type,
    sm.quantity,
    sm.reason,
    sm.created_at,
    u.email AS user_email
FROM stock_movements sm
JOIN inventory_hubs h ON sm.hub_id = h.id
JOIN items_catalog i ON sm.item_id = i.id
LEFT JOIN users u ON sm.user_id = u.id
WHERE h.name = 'Central Relief Hub' 
  AND i.code = 'WATER-001'
ORDER BY sm.created_at DESC;

-- ============================================
-- 10. SUMMARY: Total stock across all hubs
-- ============================================
SELECT 
    COUNT(DISTINCT s.hub_id) AS total_hubs,
    COUNT(DISTINCT s.item_id) AS total_items,
    COUNT(*) AS total_stock_entries,
    SUM(s.qty_available) AS total_available_stock,
    SUM(s.qty_reserved) AS total_reserved_stock,
    SUM(s.qty_available + s.qty_reserved) AS total_stock
FROM inventory_stock s;

