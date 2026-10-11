-- ============================================================================
-- BukiMind — Keycloak migration for the application database
-- ============================================================================
-- Aligns the existing application "users" table with the new User entity:
--   * users.keycloak_user_id  -> stable link to Keycloak (JWT "sub")
--   * users.full_name / avatar_url / status / last_login_at -> profile data
--   * users.password  DROPPED -> passwords belong to Keycloak only
--   * users.role      DROPPED -> authorization uses Keycloak realm roles only
--
-- Run once, against the database of the application (not the Keycloak database).
--
-- IMPORTANT — existing rows
--   Rows without a keycloak_user_id cannot be linked to a Keycloak account.
--   Either backfill them from Keycloak, or delete them before running step 4.
--   Check first:
--       SELECT id, username, email FROM users WHERE keycloak_user_id IS NULL;
--
-- Note: this file manages the CURRENT application schema (integer ids).
--       BukiMindV2.sql describes the future UUID based schema.
-- ============================================================================

BEGIN;

-- 1. stable identity ---------------------------------------------------------
ALTER TABLE users ADD COLUMN IF NOT EXISTS keycloak_user_id VARCHAR(255);

-- 2. application profile data ------------------------------------------------
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name     VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url    VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS status        VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

-- 3. default status for existing rows ---------------------------------------
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;
ALTER TABLE users ALTER COLUMN status SET NOT NULL;

-- 4. the Keycloak subject is the identity: required and unique ---------------
ALTER TABLE users ALTER COLUMN keycloak_user_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_keycloak_user_id ON users(keycloak_user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username         ON users(username);
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email            ON users(email);

-- 5. values the application writes ------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_users_status') THEN
        ALTER TABLE users
            ADD CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED'));
    END IF;
END $$;

-- 6. drop what the application no longer owns -------------------------------
ALTER TABLE users DROP COLUMN IF EXISTS password;
ALTER TABLE users DROP COLUMN IF EXISTS role;

COMMIT;
