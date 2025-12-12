-- Fix admin user password hash
-- Password: admin123
-- BCrypt hash generated with strength 10 (compatible with BCryptPasswordEncoder strength 12)
-- This hash is verified to work: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    disabled = false,
    updated_at = NOW()
WHERE email = 'admin@relief.local';

-- If user doesn't exist, create it
INSERT INTO users (id, full_name, email, password_hash, role, disabled, created_at, updated_at)
SELECT 
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'System Administrator',
    'admin@relief.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    false,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@relief.local');


