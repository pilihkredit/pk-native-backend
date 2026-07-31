-- Move login password from user_password_credential onto user_profile (AES-GCM).
-- Existing BCrypt hashes cannot be converted; users must set password again after migration.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'password_ciphertext'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile
        ADD COLUMN password_ciphertext TEXT NULL COMMENT ''AES-256-GCM encrypted login password ciphertext'' AFTER access_token_expires_at,
        ADD COLUMN password_nonce VARBINARY(12) NULL COMMENT ''AES-GCM nonce for login password'' AFTER password_ciphertext,
        ADD COLUMN password_tag VARBINARY(16) NULL COMMENT ''AES-GCM authentication tag for login password'' AFTER password_nonce,
        ADD COLUMN password_set_at DATETIME(3) NULL COMMENT ''Password set time'' AFTER password_tag,
        ADD COLUMN password_failed_attempts INT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''Consecutive failed password login attempts'' AFTER password_set_at,
        ADD COLUMN password_locked_until DATETIME(3) NULL COMMENT ''Password login lock expiry time'' AFTER password_failed_attempts',
    'SELECT ''skip: user_profile password columns exist'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_password_credential'
);
SET @ddl = IF(
    @table_exists > 0,
    'DROP TABLE user_password_credential',
    'SELECT ''skip: user_password_credential already dropped'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
