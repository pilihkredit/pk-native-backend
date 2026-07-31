-- Add last_login_at / last_logout_at on user_profile.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'last_login_at'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile ADD COLUMN last_login_at DATETIME(3) NULL COMMENT ''Latest successful login time'' AFTER password_set_at',
    'SELECT ''skip: user_profile.last_login_at exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'last_logout_at'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile ADD COLUMN last_logout_at DATETIME(3) NULL COMMENT ''Latest explicit logout time'' AFTER last_login_at',
    'SELECT ''skip: user_profile.last_logout_at exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
