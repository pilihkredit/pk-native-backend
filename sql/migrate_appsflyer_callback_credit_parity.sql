-- Align appsflyer_callback with credit: add remaining credit-only columns.
SET @schema_name = DATABASE();

SET @col = 'attributed_touch_time_selected_timezone';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN attributed_touch_time_selected_timezone VARCHAR(64) NULL COMMENT ''AF attributed_touch_time_selected_timezone'' AFTER attributed_touch_time',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'engagement_type';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN engagement_type VARCHAR(64) NULL COMMENT ''AF engagement_type'' AFTER conversion_type',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'af_attribution_lookback';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN af_attribution_lookback VARCHAR(64) NULL COMMENT ''AF af_attribution_lookback'' AFTER engagement_type',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'app_type';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN app_type VARCHAR(64) NULL COMMENT ''AF app_type'' AFTER app_name',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'is_lat';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN is_lat TINYINT(1) NULL COMMENT ''AF is_lat'' AFTER selected_currency',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'att';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN att VARCHAR(64) NULL COMMENT ''AF att'' AFTER is_lat',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'original_url';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN original_url VARCHAR(1024) NULL COMMENT ''AF original_url'' AFTER att',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'http_referrer';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN http_referrer VARCHAR(1024) NULL COMMENT ''AF http_referrer'' AFTER original_url',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'event_value';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN event_value TEXT NULL COMMENT ''AF event_value JSON/string'' AFTER http_referrer',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'event_value_app_id';
SET @ddl = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='appsflyer_callback' AND COLUMN_NAME=@col)=0,
  'ALTER TABLE appsflyer_callback ADD COLUMN event_value_app_id VARCHAR(128) NULL COMMENT ''app_id extracted from event_value'' AFTER event_value',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;
