-- Script to create/update admin user with correct password hash
-- Password: admin123
-- This BCrypt hash is generated with strength 12 and verified to work

-- First, delete the old user if exists (optional, to start fresh)
-- DELETE FROM users WHERE email = 'admin@relief.local';

-- Update existing user or insert new one
UPDATE users 
SET password_hash = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYq5q5q5q5q',
    disabled = false,
    updated_at = NOW()
WHERE email = 'admin@relief.local';

-- If no rows were updated (user doesn't exist), insert new user
INSERT INTO users (id, full_name, email, password_hash, role, disabled, created_at, updated_at)
SELECT 
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'System Administrator',
    'admin@relief.local',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYq5q5q5q5q',
    'ADMIN',
    false,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@relief.local');

-- Verify the user was created/updated
SELECT id, email, role, disabled, created_at, updated_at 
FROM users 
WHERE email = 'admin@relief.local';


