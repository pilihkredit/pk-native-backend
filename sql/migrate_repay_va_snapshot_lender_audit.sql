-- Add lender VA-list audit columns to repay_va_snapshot.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'repay_va_snapshot'
      AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE repay_va_snapshot ADD COLUMN last_lender_request_json JSON NULL COMMENT ''Last lender VA list request JSON'' AFTER bank_channels_json',
    'SELECT ''skip: repay_va_snapshot.last_lender_request_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'repay_va_snapshot'
      AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE repay_va_snapshot ADD COLUMN last_lender_response_json JSON NULL COMMENT ''Last lender VA list response JSON'' AFTER last_lender_request_json',
    'SELECT ''skip: repay_va_snapshot.last_lender_response_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
