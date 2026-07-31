-- Add user_device.ip for client-reported IP (lender device.ip).
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'ip'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_device ADD COLUMN ip VARCHAR(64) NULL COMMENT ''Client IP (lender device.ip)'' AFTER ext_param',
    'SELECT ''skip: user_device.ip exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
