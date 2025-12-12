-- Fix the log_status_change function to handle different table structures
CREATE OR REPLACE FUNCTION log_status_change()
RETURNS TRIGGER AS $$
DECLARE
    user_id UUID;
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.status != NEW.status THEN
        -- Determine the user_id based on the table
        IF TG_TABLE_NAME = 'tasks' THEN
            user_id := NEW.assignee_id;
        ELSIF TG_TABLE_NAME = 'needs_requests' THEN
            user_id := NEW.current_assignee_id;
        ELSE
            user_id := NULL;
        END IF;
        
        INSERT INTO status_history (entity_type, entity_id, from_status, to_status, by_user_id, created_at)
        VALUES (TG_TABLE_NAME, NEW.id, OLD.status, NEW.status, user_id, NOW());
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;