-- Drop unused user_profile columns: retention_policy_code, anonymized_at.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'retention_policy_code'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE user_profile DROP COLUMN retention_policy_code',
    'SELECT ''skip: user_profile.retention_policy_code absent'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'anonymized_at'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE user_profile DROP COLUMN anonymized_at',
    'SELECT ''skip: user_profile.anonymized_at absent'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
