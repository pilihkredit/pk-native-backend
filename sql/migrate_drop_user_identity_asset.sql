-- Drop legacy identity asset table after migrate_ocr_identity_two_table.sql
-- has backfilled user_profile_identity and the new app build is live.
-- Safe to re-run. Irreversible — take a backup if needed.

SET @db = DATABASE();

-- Prefer dropping the renamed backup table.
SET @deprecated_exists = (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_identity_asset_deprecated'
);
SET @ddl = IF(
    @deprecated_exists > 0,
    'DROP TABLE user_identity_asset_deprecated',
    'SELECT ''skip: user_identity_asset_deprecated missing'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- If rename was skipped and the old name still exists (and app no longer uses it).
SET @asset_exists = (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_identity_asset'
);
SET @ddl = IF(
    @asset_exists > 0,
    'DROP TABLE user_identity_asset',
    'SELECT ''skip: user_identity_asset missing'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
