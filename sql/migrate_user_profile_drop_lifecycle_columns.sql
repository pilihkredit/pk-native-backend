-- Drop unused user_profile columns: current_profile_version_id, data_lifecycle_status, data_residency_country.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND INDEX_NAME = 'idx_user_profile_lifecycle_retention'
);
SET @ddl = IF(
    @idx_exists > 0,
    'ALTER TABLE user_profile DROP INDEX idx_user_profile_lifecycle_retention',
    'SELECT ''skip: idx_user_profile_lifecycle_retention absent'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND INDEX_NAME = 'idx_user_profile_retention_until'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_retention_until ON user_profile (retention_until)',
    'SELECT ''skip: idx_user_profile_retention_until exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'current_profile_version_id'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE user_profile DROP COLUMN current_profile_version_id',
    'SELECT ''skip: user_profile.current_profile_version_id absent'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'data_lifecycle_status'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE user_profile DROP COLUMN data_lifecycle_status',
    'SELECT ''skip: user_profile.data_lifecycle_status absent'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'data_residency_country'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE user_profile DROP COLUMN data_residency_country',
    'SELECT ''skip: user_profile.data_residency_country absent'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
