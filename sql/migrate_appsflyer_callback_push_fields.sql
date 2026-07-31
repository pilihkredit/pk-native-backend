-- Add AF Push fields present in real callbacks but not in the initial lender-field set.
SET @schema_name = DATABASE();

SET @cols = CONCAT_WS(',',
  'ADD COLUMN event_source VARCHAR(64) NULL COMMENT ''AF event_source'' AFTER gp_install_begin',
  'ADD COLUMN event_time VARCHAR(64) NULL COMMENT ''AF event_time'' AFTER event_source',
  'ADD COLUMN event_time_selected_timezone VARCHAR(64) NULL COMMENT ''AF event_time_selected_timezone'' AFTER event_time',
  'ADD COLUMN app_name VARCHAR(256) NULL COMMENT ''AF app_name'' AFTER event_time_selected_timezone',
  'ADD COLUMN campaign_type VARCHAR(64) NULL COMMENT ''AF campaign_type'' AFTER app_name',
  'ADD COLUMN conversion_type VARCHAR(64) NULL COMMENT ''AF conversion_type'' AFTER campaign_type',
  'ADD COLUMN is_retargeting TINYINT(1) NULL COMMENT ''AF is_retargeting'' AFTER conversion_type',
  'ADD COLUMN region VARCHAR(64) NULL COMMENT ''AF region'' AFTER is_retargeting',
  'ADD COLUMN state VARCHAR(64) NULL COMMENT ''AF state'' AFTER region',
  'ADD COLUMN dma VARCHAR(64) NULL COMMENT ''AF dma'' AFTER state',
  'ADD COLUMN wifi TINYINT(1) NULL COMMENT ''AF wifi'' AFTER dma',
  'ADD COLUMN carrier VARCHAR(128) NULL COMMENT ''AF carrier'' AFTER wifi',
  'ADD COLUMN language VARCHAR(64) NULL COMMENT ''AF language'' AFTER carrier',
  'ADD COLUMN install_time_selected_timezone VARCHAR(64) NULL COMMENT ''AF install_time_selected_timezone'' AFTER language',
  'ADD COLUMN device_download_time VARCHAR(64) NULL COMMENT ''AF device_download_time'' AFTER install_time_selected_timezone',
  'ADD COLUMN device_download_time_selected_timezone VARCHAR(64) NULL COMMENT ''AF device_download_time_selected_timezone'' AFTER device_download_time',
  'ADD COLUMN gp_referrer VARCHAR(1024) NULL COMMENT ''AF gp_referrer'' AFTER device_download_time_selected_timezone',
  'ADD COLUMN sdk_version VARCHAR(64) NULL COMMENT ''AF sdk_version'' AFTER gp_referrer',
  'ADD COLUMN api_version VARCHAR(32) NULL COMMENT ''AF api_version'' AFTER sdk_version',
  'ADD COLUMN user_agent VARCHAR(512) NULL COMMENT ''AF user_agent'' AFTER api_version',
  'ADD COLUMN selected_timezone VARCHAR(64) NULL COMMENT ''AF selected_timezone'' AFTER user_agent',
  'ADD COLUMN selected_currency VARCHAR(16) NULL COMMENT ''AF selected_currency'' AFTER selected_timezone'
);

-- Add each column if missing (idempotent).
SET @col = 'event_source';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN event_source VARCHAR(64) NULL COMMENT ''AF event_source'' AFTER gp_install_begin',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'event_time';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN event_time VARCHAR(64) NULL COMMENT ''AF event_time'' AFTER event_source',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'event_time_selected_timezone';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN event_time_selected_timezone VARCHAR(64) NULL COMMENT ''AF event_time_selected_timezone'' AFTER event_time',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'app_name';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN app_name VARCHAR(256) NULL COMMENT ''AF app_name'' AFTER event_time_selected_timezone',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'campaign_type';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN campaign_type VARCHAR(64) NULL COMMENT ''AF campaign_type'' AFTER app_name',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'conversion_type';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN conversion_type VARCHAR(64) NULL COMMENT ''AF conversion_type'' AFTER campaign_type',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'is_retargeting';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN is_retargeting TINYINT(1) NULL COMMENT ''AF is_retargeting'' AFTER conversion_type',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'region';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN region VARCHAR(64) NULL COMMENT ''AF region'' AFTER is_retargeting',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'state';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN state VARCHAR(64) NULL COMMENT ''AF state'' AFTER region',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'dma';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN dma VARCHAR(64) NULL COMMENT ''AF dma'' AFTER state',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'wifi';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN wifi TINYINT(1) NULL COMMENT ''AF wifi'' AFTER dma',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'carrier';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN carrier VARCHAR(128) NULL COMMENT ''AF carrier'' AFTER wifi',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'language';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN language VARCHAR(64) NULL COMMENT ''AF language'' AFTER carrier',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'install_time_selected_timezone';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN install_time_selected_timezone VARCHAR(64) NULL COMMENT ''AF install_time_selected_timezone'' AFTER language',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'device_download_time';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN device_download_time VARCHAR(64) NULL COMMENT ''AF device_download_time'' AFTER install_time_selected_timezone',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'device_download_time_selected_timezone';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN device_download_time_selected_timezone VARCHAR(64) NULL COMMENT ''AF device_download_time_selected_timezone'' AFTER device_download_time',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'gp_referrer';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN gp_referrer VARCHAR(1024) NULL COMMENT ''AF gp_referrer'' AFTER device_download_time_selected_timezone',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'sdk_version';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN sdk_version VARCHAR(64) NULL COMMENT ''AF sdk_version'' AFTER gp_referrer',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'api_version';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN api_version VARCHAR(32) NULL COMMENT ''AF api_version'' AFTER sdk_version',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'user_agent';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN user_agent VARCHAR(512) NULL COMMENT ''AF user_agent'' AFTER api_version',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'selected_timezone';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN selected_timezone VARCHAR(64) NULL COMMENT ''AF selected_timezone'' AFTER user_agent',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'selected_currency';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN selected_currency VARCHAR(16) NULL COMMENT ''AF selected_currency'' AFTER selected_timezone',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;
