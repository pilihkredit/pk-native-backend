-- Drop password lock fields from user_profile.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'password_failed_attempts'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE user_profile DROP COLUMN password_failed_attempts',
    'SELECT ''skip: password_failed_attempts absent'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'password_locked_until'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE user_profile DROP COLUMN password_locked_until',
    'SELECT ''skip: password_locked_until absent'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
