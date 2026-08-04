-- Add password change timestamp while preserving the first password setup timestamp.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'password_changed_at'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile ADD COLUMN password_changed_at DATETIME(3) NULL COMMENT ''Latest password change time'' AFTER password_set_at',
    'SELECT ''skip: user_profile.password_changed_at exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
