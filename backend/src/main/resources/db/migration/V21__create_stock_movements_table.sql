-- Create stock movements table for tracking inventory changes
CREATE TABLE IF NOT EXISTS stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_id UUID REFERENCES inventory_hubs(id),
    item_id UUID REFERENCES items_catalog(id),
    movement_type VARCHAR(50) NOT NULL, -- 'in', 'out', 'reserve', 'release', 'adjustment'
    quantity INTEGER NOT NULL,
    reason TEXT,
    user_id UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create indexes for stock movements
CREATE INDEX IF NOT EXISTS idx_stock_movements_hub_item ON stock_movements (hub_id, item_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_type ON stock_movements (movement_type);
CREATE INDEX IF NOT EXISTS idx_stock_movements_created_at ON stock_movements (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_movements_user ON stock_movements (user_id);

-- Add constraint for movement types
ALTER TABLE stock_movements ADD CONSTRAINT check_movement_type 
    CHECK (movement_type IN ('in', 'out', 'reserve', 'release', 'adjustment'));

