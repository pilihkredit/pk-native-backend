-- Expand user_device columns to cover lender userInfo.device top-level fields.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

-- Rename client_app_name -> app_name (lender appName)
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'client_app_name'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE user_device CHANGE COLUMN client_app_name app_name VARCHAR(64) NOT NULL COMMENT ''Lender appName''',
    'SELECT ''skip: user_device.client_app_name already renamed or missing'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'app_name'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_device ADD COLUMN app_name VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''Lender appName'' AFTER system_platform',
    'SELECT ''skip: user_device.app_name exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'phone_brand');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN phone_brand VARCHAR(64) NULL COMMENT ''phoneBrand'' AFTER package_name', 'SELECT ''skip: user_device.phone_brand exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'phone_brand_model');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN phone_brand_model VARCHAR(64) NULL COMMENT ''phoneBrandModel'' AFTER phone_brand', 'SELECT ''skip: user_device.phone_brand_model exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'mac');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN mac VARCHAR(64) NULL COMMENT ''mac'' AFTER phone_brand_model', 'SELECT ''skip: user_device.mac exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'system_version');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN system_version VARCHAR(32) NULL COMMENT ''systemVersion'' AFTER mac', 'SELECT ''skip: user_device.system_version exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'delivery_platform');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN delivery_platform VARCHAR(64) NULL COMMENT ''deliveryPlatform'' AFTER system_version', 'SELECT ''skip: user_device.delivery_platform exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'cpu_cores');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN cpu_cores INT NULL COMMENT ''cpuCores'' AFTER delivery_platform', 'SELECT ''skip: user_device.cpu_cores exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'memory_total');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN memory_total BIGINT NULL COMMENT ''memoryTotal'' AFTER cpu_cores', 'SELECT ''skip: user_device.memory_total exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'sd_card_total');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN sd_card_total BIGINT NULL COMMENT ''sdCardTotal'' AFTER memory_total', 'SELECT ''skip: user_device.sd_card_total exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'idfv');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN idfv VARCHAR(64) NULL COMMENT ''idfv'' AFTER ad_id', 'SELECT ''skip: user_device.idfv exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'idfa');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN idfa VARCHAR(64) NULL COMMENT ''idfa'' AFTER idfv', 'SELECT ''skip: user_device.idfa exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_device' AND COLUMN_NAME = 'ext_param');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_device ADD COLUMN ext_param VARCHAR(1024) NULL COMMENT ''extParam'' AFTER idfa', 'SELECT ''skip: user_device.ext_param exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
