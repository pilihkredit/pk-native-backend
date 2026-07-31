-- Persist client device_no on AF rows for identity companion lookup.
-- Nullable for legacy rows; new inserts require non-blank device_no.
-- Safe to re-run. MySQL 5.7 / 8.0.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_af'
      AND COLUMN_NAME = 'device_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_af ADD COLUMN device_no VARCHAR(64) NULL COMMENT ''Client device number from X-Device-No'' AFTER mobile_no',
    'SELECT ''skip: user_profile_af.device_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_af'
      AND INDEX_NAME = 'idx_user_profile_af_device'
);
SET @ddl_idx = IF(
    @idx_exists = 0,
    'ALTER TABLE user_profile_af ADD KEY idx_user_profile_af_device (device_no, created_at)',
    'SELECT ''skip: idx_user_profile_af_device exists'' AS migration_info'
);
PREPARE stmt FROM @ddl_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
