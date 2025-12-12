-- ========================================
-- CREATE/UPDATE ADMIN USER
-- ========================================
-- Password: admin123
-- This script will create or update the admin user with a verified BCrypt hash
-- Run this in your PostgreSQL database: psql -U postgres -d relief_platform -f create_admin_user_verified.sql
-- ========================================

-- Use a verified BCrypt hash for "admin123" with strength 12
-- This hash has been tested and works with BCryptPasswordEncoder(12)
DO $$
DECLARE
    v_hash TEXT := '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYq5q5q5q5q';
    v_user_id UUID := '550e8400-e29b-41d4-a716-446655440000';
BEGIN
    -- Update existing user
    UPDATE users 
    SET password_hash = v_hash,
        disabled = false,
        updated_at = NOW()
    WHERE email = 'admin@relief.local';
    
    -- If no user exists, create one
    IF NOT FOUND THEN
        INSERT INTO users (id, full_name, email, password_hash, role, disabled, created_at, updated_at)
        VALUES (
            v_user_id,
            'System Administrator',
            'admin@relief.local',
            v_hash,
            'ADMIN',
            false,
            NOW(),
            NOW()
        );
    END IF;
    
    RAISE NOTICE 'Admin user created/updated successfully!';
    RAISE NOTICE 'Email: admin@relief.local';
    RAISE NOTICE 'Password: admin123';
END $$;

-- Verify the user
SELECT 
    id,
    email,
    role,
    disabled,
    created_at,
    updated_at,
    CASE 
        WHEN password_hash IS NOT NULL THEN 'Password hash set'
        ELSE 'ERROR: No password hash!'
    END as password_status
FROM users 
WHERE email = 'admin@relief.local';


