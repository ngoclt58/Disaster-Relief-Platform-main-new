-- Run this SQL in your PostgreSQL database to fix the Flyway migration issue
-- Connect to your database: psql -U postgres -d relief_platform

-- Delete the V19 migration record from Flyway history
DELETE FROM flyway_schema_history WHERE version = '19';

-- Verify it's deleted
SELECT version, description, installed_on FROM flyway_schema_history WHERE version = '19';


