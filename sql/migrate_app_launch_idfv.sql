-- Add optional idfv to app_launch_event.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'app_launch_event'
      AND COLUMN_NAME = 'idfv'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE app_launch_event ADD COLUMN idfv VARCHAR(128) NULL COMMENT ''iOS IDFV'' AFTER app_package',
    'SELECT ''skip: app_launch_event.idfv exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
