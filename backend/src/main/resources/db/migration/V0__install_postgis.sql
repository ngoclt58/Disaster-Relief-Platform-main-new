-- ============================================================================
-- PostGIS Extension Installation (OPTIONAL)
-- ============================================================================
-- This migration attempts to enable the PostGIS extension.
-- 
-- IMPORTANT: PostGIS must be installed on your PostgreSQL server before
-- running this migration. If you see an error about PostGIS not being
-- available, you have two options:
--
-- OPTION 1: Install PostGIS (Recommended for production)
--   - Ubuntu/Debian: sudo apt-get install postgresql-postgis
--   - CentOS/RHEL: sudo yum install postgis
--   - macOS (Homebrew): brew install postgis
--   - Windows: Install PostGIS from https://postgis.net/windows_downloads/
--
-- OPTION 2: Skip PostGIS (For development only)
--   - Comment out or delete the CREATE EXTENSION line below
--   - Use V1__baseline_no_postgis.sql instead of V1__baseline.sql
--   - Note: You'll lose spatial query capabilities (distance, contains, etc.)
--
-- After installing PostGIS, restart PostgreSQL and run migrations again.
-- ============================================================================

-- Create PostGIS extension
-- Uncomment the line below after installing PostGIS on your PostgreSQL server
-- See INSTALL_POSTGIS.md for installation instructions
CREATE EXTENSION IF NOT EXISTS postgis;

